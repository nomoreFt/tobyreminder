import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

// 이 파일은 "코루틴이 실제로 어떻게 움직이는지"를 콘솔 로그로 확인하기 위한 샘플이다.
// 각 데모는 하나의 핵심 개념만 분리해서 보여준다.
//
// 실행할 때 볼 포인트:
// 1. 같은 코드처럼 보여도 suspension(delay)와 blocking(Thread.sleep)은 동작 방식이 다르다.
// 2. coroutineScope는 자식 실패를 부모와 형제에게 전파한다.
// 3. supervisorScope는 자식 실패를 다른 형제에게 전파하지 않는다.
// 4. Dispatchers는 "어떤 스레드/스레드풀에서 일할지"를 결정한다.
// 5. cancel은 즉시 강제 종료가 아니라, 취소 가능한 지점에서 협조적으로 반영된다.

suspend fun main() {
    println("main thread = ${Thread.currentThread().name}")

    demoLaunchAndAsync()
    demoDelayVsSleep()
    demoDispatcherAndWithContext()
    demoCancellation()
    demoFailurePropagation()
    demoSupervisorScope()
    demoAsyncInCoroutineScope()
    demoAsyncInSupervisorScope()
}

private suspend fun demoLaunchAndAsync() = coroutineScope {
    // coroutineScope는 이 블록 안의 자식 코루틴이 모두 끝날 때까지 반환되지 않는다.
    // 즉, 구조화된 동시성(structured concurrency)의 기본 단위다.
    printSection("launch vs async")

    // launch는 결과값이 없는 fire-and-wait 작업에 적합하다.
    // 반환 타입은 Job이고, 완료 여부/취소 여부를 추적하는 용도다.
    val launchedJob = launch {
        repeat(3) { index ->
            println("launch tick $index on ${Thread.currentThread().name}")
            delay(150)
        }
    }

    // async는 결과값이 필요한 비동기 작업에 쓴다.
    // 반환 타입은 Deferred<T>이고, await()로 결과를 꺼낸다.
    // 주의: 결과를 쓸 계획이 없으면 async보다 launch가 더 명확하다.
    val deferred = async {
        delay(250)
        println("async finished on ${Thread.currentThread().name}")
        42
    }

    // await()는 결과가 준비될 때까지 현재 코루틴을 일시중단한다.
    // 스레드를 막는 것이 아니라 코루틴만 잠시 멈춘다.
    println("awaiting async result")
    println("async result = ${deferred.await()}")
    launchedJob.join()
}

private suspend fun demoDelayVsSleep() = coroutineScope {
    // delay는 non-blocking suspension이다.
    // Thread.sleep은 현재 스레드를 그대로 붙잡아 둔다.
    //
    // 이 예제는 둘 다 같은 scope 안에서 돌려 보고, 시간 로그가 어떻게 달라지는지 확인하는 용도다.
    // 실제 서비스 코드에서 코루틴 안에 Thread.sleep을 넣으면 스레드 낭비가 생기므로 보통 피한다.
    printSection("delay vs Thread.sleep")

    val start = System.currentTimeMillis()

    val delayedJob = launch {
        repeat(3) { index ->
            delay(100)
            println("delay coroutine tick $index at ${System.currentTimeMillis() - start}ms")
        }
    }

    val sleepingJob = launch {
        // 이 코드는 교육용이다.
        // 코루틴을 쓰는 목적 중 하나가 "적은 스레드로 많은 대기 작업을 처리"하는 것인데,
        // Thread.sleep은 그 장점을 무력화한다.
        repeat(3) { index ->
            Thread.sleep(100)
            println("sleep coroutine tick $index at ${System.currentTimeMillis() - start}ms")
        }
    }

    delayedJob.join()
    sleepingJob.join()
}

private suspend fun demoDispatcherAndWithContext() = coroutineScope {
    // Dispatcher는 코루틴이 어느 실행 컨텍스트에서 돌아갈지 정한다.
    // 흔히 Default는 CPU 연산, IO는 블로킹 IO 분리 용도로 사용한다.
    printSection("dispatchers and withContext")

    println("scope thread = ${Thread.currentThread().name}")

    // launch에 Dispatcher를 지정하면 새로운 코루틴을 해당 실행 컨텍스트에서 시작한다.
    launch(Dispatchers.Default) {
        println("default dispatcher = ${Thread.currentThread().name}")
    }.join()

    // withContext는 "새 코루틴을 만드는 것"보다 "현재 suspend 흐름의 컨텍스트를 잠시 바꾸는 것"에 가깝다.
    // 반환값을 자연스럽게 받기 좋아서, suspend 함수 내부에서 컨텍스트 전환할 때 자주 쓴다.
    val value = withContext(Dispatchers.IO) {
        println("withContext(IO) = ${Thread.currentThread().name}")
        "io-result"
    }

    println("back to caller with value = $value on ${Thread.currentThread().name}")
}

private suspend fun demoCancellation() = coroutineScope {
    // 코루틴 취소는 선점형이 아니라 협조적(cooperative)이다.
    // 즉, 코드가 취소를 확인하거나(delay 같은) 취소 가능한 suspend 지점을 지나야 반영된다.
    printSection("cancellation")

    val job = launch {
        try {
            // isActive를 확인하면 긴 루프에서도 취소 요청을 감지할 수 있다.
            // CPU 바운드 루프라면 yield()나 isActive 체크를 직접 넣어줘야 할 수 있다.
            while (currentCoroutineContext().isActive) {
                println("working on ${Thread.currentThread().name}")
                delay(120)
            }
        } catch (e: CancellationException) {
            // CancellationException은 "정상적인 취소 신호"다.
            // 실무에서는 이 예외를 삼켜 버리면 상위 취소 전파가 끊길 수 있으니 보통 다시 던진다.
            println("cancellation exception observed")
            throw e
        } finally {
            // finally는 취소 시에도 호출된다.
            // 자원 정리, 로그, close 처리 등을 두기 좋다.
            println("finally after cancellation")
        }
    }

    delay(350)
    println("cancel job")
    job.cancelAndJoin()
}

private suspend fun demoFailurePropagation() = try {
    // coroutineScope에서는 자식 하나가 실패하면 같은 scope의 다른 자식도 함께 취소된다.
    // 이것이 기본 동작이고, "한 작업 단위는 같이 성공하거나 같이 실패"하게 만든다.
    coroutineScope {
        printSection("coroutineScope failure propagation")

        launch {
            try {
                repeat(5) { index ->
                    delay(100)
                    println("survivor tick $index")
                }
            } finally {
                // 형제 코루틴이 실패하면 이 코루틴도 취소되므로 finally가 실행된다.
                println("survivor cancelled by sibling failure")
            }
        }

        launch {
            // 예외가 발생하면 부모 scope가 실패하고, 다른 자식에게 취소가 전파된다.
            delay(220)
            error("boom in coroutineScope")
        }
    }
} catch (e: IllegalStateException) {
    println("caught scope failure: ${e.message}")
}

private suspend fun demoSupervisorScope() = supervisorScope {
    // supervisorScope는 자식 실패를 다른 형제에게 전파하지 않는다.
    // 여러 독립 작업 중 하나가 실패해도 나머지는 계속 처리하고 싶을 때 쓴다.
    //
    // 단, "완전히 예외가 사라지는 것"은 아니다.
    // 실패한 자식 자체는 여전히 실패 상태이고, 그 결과를 따로 관찰/처리해야 한다.
    printSection("supervisorScope")

    val failingJob = launch {
        delay(150)
        error("boom in supervisorScope")
    }

    val siblingJob = launch {
        repeat(4) { index ->
            delay(100)
            println("sibling survived tick $index")
        }
    }

    // launch의 예외는 Deferred처럼 await로 받는 구조가 아니라 Job 완료 상태에 실린다.
    // invokeOnCompletion으로 실패 여부를 관찰해 본다.
    failingJob.invokeOnCompletion { throwable ->
        println("failing job completed with ${throwable?.javaClass?.simpleName}: ${throwable?.message}")
    }

    // join은 "끝날 때까지 기다리는 것"이지, 예외를 다시 던지는 API는 아니다.
    // 그래서 failingJob이 죽어도 siblingJob은 계속 진행된다.
    failingJob.join()
    siblingJob.join()
    println("supervisorScope kept sibling alive")
}

private suspend fun demoAsyncInCoroutineScope() {
    // async에서는 예외가 바로 바깥으로 튀어나오는 대신 Deferred 안에 저장된다.
    // 하지만 coroutineScope 안에서 자식 async 하나가 실패하면, 부모 scope 자체는 실패 상태가 되고
    // 다른 형제 async도 취소된다. 즉 "실패 전파 정책"은 그대로 유지된다.
    try {
        coroutineScope {
            printSection("async in coroutineScope")

            val slowSuccess = async {
                try {
                    repeat(5) { index ->
                        delay(100)
                        println("slow success tick $index")
                    }
                    "success"
                } finally {
                    println("slow success cancelled because sibling failed")
                }
            }

            val fastFailure = async<String> {
                delay(220)
                error("boom from async in coroutineScope")
            }

            // fastFailure.await()에서 예외가 드러난다.
            // 그 시점에 같은 scope의 slowSuccess는 이미 취소 전파를 받는다.
            println("fastFailure result = ${fastFailure.await()}")
            println("slowSuccess result = ${slowSuccess.await()}")
        }
    } catch (e: IllegalStateException) {
        println("caught async coroutineScope failure: ${e.message}")
    }
}

private suspend fun demoAsyncInSupervisorScope() = supervisorScope {
    // supervisorScope + async 조합에서는 각 Deferred를 개별적으로 await/처리할 수 있다.
    // 하나가 실패해도 다른 Deferred는 계속 진행되므로, 부분 성공을 수집하기 좋다.
    printSection("async in supervisorScope")

    val slowSuccess = async {
        repeat(5) { index ->
            delay(100)
            println("supervised success tick $index")
        }
        "success"
    }

    val fastFailure = async<String> {
        delay(220)
        error("boom from async in supervisorScope")
    }

    // supervisorScope에서는 실패 격리가 되므로, 실패한 Deferred와 성공한 Deferred를 각각 다룰 수 있다.
    val failed = runCatching { fastFailure.await() }
    val succeeded = runCatching { slowSuccess.await() }

    println("fastFailure await result = $failed")
    println("slowSuccess await result = $succeeded")
}

private fun printSection(title: String) {
    println()
    println("=== $title ===")
}

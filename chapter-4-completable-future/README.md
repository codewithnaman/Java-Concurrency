# Table Of Contents
* [Future and its shortcoming](#futuret-in-java-and-shortcoming-with-it)
* [CompletableFuture](#completablefuture)
    * [Running a task in different thread](#lets-see-how-to-submit-a-task-to-completablefuture-and-use-of-it)
    * [thenAccept to get Result](#thenaccept-method-of-completablefuture)
    * [thenAcceptAsync method](#thenacceptasync-method-of-completablefuture)
    * [thenApply to transform result from CompletableFuture](#thenapply-or-thenapplyasync-method-of-completablefuture)
    * [thenRun method](#thenrun-method-of-completablefuture)
    * [Completing CompletableFuture from Code](#completed-method-of-completablefuture)
    * [Cancelling CompletableFuture from Code](#cancelling-the-completablefuture)
    * [Handling Exception or Failure in CompletableFuture](#handling-exception-and-failure-of-completablefuture)
* [Working with Multiple CompletableFuture](#working-with-multiple-completablefuture)
    * [thenCombine method to merge result](#thencombine-method)
    * [Handling return type CompletableFuture from a Completable Future using thenCompose method](#thencompose-method)
    * [acceptEither or applyToEither](#accepteither-and-applytoeither-method)
    * [runAfterEither and runAfterBoth method](#runafter-methods) 
    * [anyOfMethod](#anyof-method)
    * [Handling timeout till Java 8](#timeout-in-case-of-multiple-completablefuture)
    * [allOf method](#allof-method)
* [Java 9 timeout improvement for CompletableFuture](#java-9-timeout-method)

# Completable Future
## Future<T> in java and shortcoming with it
Let's first talk about Future in Java and why we need Completable Future. Future helps to get the result after task 
execution completed. To Create a task we submit it to executor pool which return us Future<T> for getting the results.
We execute the task and to get the result we call the get method which is blocking call for the method from which we
are calling, or we can use isDone method for checking if the task has been completed, then we can use get call to 
get the result after task execution. But this is not best practice as our task is executing on another thread, and we
are waiting for the task to be completed on other thread. Let's see below example:
```
public static int task() {
        System.out.println("Task Started...");
        sleep(10000);
        System.out.println("Task Completed...");
        return 1;
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        Future<Integer> taskResult = executorService.submit(Example1Future::task);
        System.out.println("Some other instructions performed");
        int result = taskResult.get();
        System.out.println("Task Result After execution " + result);
        executorService.shutdown();
    }
```

In above example when we give get call on taskResult; it will block the execution of main thread and wait for the
task to be completed on the executor pool. Even our task is running asynchronously but, we are waiting in main thread
as synchronous.

Another drawback of Future is in case of ExceptionHandling when, there are multiple jobs submitted to executor pool. 
Just consider we submitted multiple jobs to executor pool, and we want result to process after the job will be done;
For this we need to store Future<T> object in a list; Which will process the result sequentially and just consider
our first task is taking much time and later jobs completed; But our cursor will still in blocked condition because
of the get call performed on the 0th task of list which is blocking call. Due to this the results will be processed 
sequentially and main thread wait again for completion of other tasks.

Also, with multiple tasks handling the exception handling of each task will be difficult; just consider you have 
submitted 10 jobs and 2 of them are fail and throw the exception, then we need to put each task handling inside
the loop which is processing the results. If you put the try-catch over the for loop, and a task has failed then 
rest of results we are not able to process. 

Let's see an example of this below:
```text
    public static int task(int max, int index) {
        System.out.println("Task Started..." + index);
        sleep((max - index) * 1000);
      /*  if (resultNumber > 1000) {
            throw new RuntimeException("This Operation Can not be completed");
        }*/
        System.out.println("Task Completed..." + index);
        return index;
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        int max = 10;
        List<Future<Integer>> results = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            int index = i;
            results.add(executorService.submit(() -> task(max, index)));
        }
        System.out.println("Some other instructions performed");
        for (Future<Integer> result : results) {
            System.out.println(result.get());
        }
        executorService.shutdown();
    }
```

In above example we can see that even if our last task finished we will still wait for the first task finished as in the
main thread we are trying to get the first task result first. Also, if uncomment the lines from task then we need to 
handle the exception which we need to do inside the for loop if we put it outside then if first task has failed then it
will not process further for results. We need to perform checks before making get call still it will not efficient as
we are waiting for first task to finish or sequentially it needs to finish.

## CompletableFuture
This is introduced as part of Java 8. CompletableFuture provides the much better solution than Future and provides
the solution of problems we discussed in above section. This provides us facility to run the for both task and main 
thread asynchronously.

If you are familiar with JavaScript then; you can say CompletableFuture is promises in Java. CompletableFuture is 
pipeline of the states; If one stage completes then result moves to next state to run. 

Let's understand first how CompletableFuture and promise works. Just consider we have a task to complete; then it 
provides two channels; one for the data if execution is succeed; other is for the error if occurred during execution 
of task. 

### Let's see how to submit a task to CompletableFuture and use of it
To Submit a Runnable to run in a different thread we put them in runAsync method of CompletableFuture and since Runnable
do not return result we will not much concern. Below is one example for this:
```text
    public static void task() {
        System.out.println("Task Started..." + Thread.currentThread());
        sleep(1000);
        System.out.println("Task Completed..." + Thread.currentThread());
    }

    public static void main(String[] args) {
        CompletableFuture.runAsync(() -> task());
        sleep(2000);
        System.out.println("Main completed");
    }
```
In Above example we run the task using runAsync method. Now question is in which pool this thread will run; Answer to this
is that uses the ForkJoinCommonPool; If we want to run in different pool then we can pass our pool parameter to runAsync
method as second parameter of method as runAsync method is overloaded in CompletableFuture.

Just consider we want result after method complete then we need to use different method of the CompletableFuture which
is supplyAsync which takes a supplier; and Since, CompletableFuture is extended from the Future we can perform the get
call on this. Like below:
```text
    public static int task() {
        System.out.println("Task Started..." + Thread.currentThread());
        sleep(1000);
        System.out.println("Task Completed..." + Thread.currentThread());
        return 1;
    }

    public static void main(String[] args) throws Exception{
        CompletableFuture<Integer> result = CompletableFuture.supplyAsync(() -> task());
        System.out.println("Main Completed");
        System.out.println("Result from the task is " + result.get());
    }
```
But, get is again blocking call as Future, so CompletableFuture provides one more method to get the result which is
getNow which takes an argument and if the task is completed, then it returns the result which it got after task 
completion otherwise the argument we passed as getNow method. Like below example:
```text
    public static void main(String[] args) throws Exception{
        CompletableFuture<Integer> result = CompletableFuture.supplyAsync(() -> task());
        System.out.println("Main Completed");
        System.out.println("Result from the task is " + result.getNow(-1));
    }
```
So, if the task is completed in above call, then it will print 1 otherwise it will print -1.

#### thenAccept method of CompletableFuture
In getNow call we are returning the result if the result is available, otherwise we are returning default value passed
as argument of that method. Now consider we have to consume the result whenever it is available. For this case we have
thenAccept method of CompletableFuture which takes Consumer as argument which means; it consumes the data whatever the
data generated by after execution of CompletableFuture. Let's see below example for this:
```text
public class Example2ThenAcceptCompletableFuture {

    public static int supplierFunction() {
       // sleep(1000);
        System.out.println("Generating Value " + Thread.currentThread());
        return 5;
    }

    public static void consumer(int number) {
        System.out.println("Printing the Number " + Thread.currentThread());
        System.out.println("Number is " + number);
    }

    public static void main(String[] args) {
               CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Example2ThenAcceptCompletableFuture::supplierFunction);
               System.out.println("Registering then Accept");
               future.thenAccept(Example2ThenAcceptCompletableFuture::consumer);
               System.out.println("Main Almost Completed");
               sleep(4000);
    }
}
``` 

In above example we have called the supplyAsync method which will execute the task in common pool as discussed in above 
sections. Then we are calling thenAccept method of CompletableFuture in which we are passing the consumer as function,
If result is available it will execute the function immediately in the caller thread otherwise when the CompletableFuture 
completes its task and thenAccept will execute in the same thread on which task has executed. For example in our above 
example output will be:
```text
Registering then Accept
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
Printing the Number Thread[main,5,main]
Number is 5
Main Almost Completed
```
As we can see the task has executed in common ForkJoinPool; But the Consumer has executed in the main thread which is 
the caller thread since result is available. While if we uncomment the method sleep in supplier which provides the task
to CompletableFuture; then result will be different. Let's see what is output when we uncomment the sleep:
```text
Registering then Accept
Main Almost Completed
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
Printing the Number Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number is 5
```
Since the result is not available by the time thenAccept is registered, and it completed at later point of time; so it 
will execute thenAccept Consumer in the thread in which task has completed.

But this is inconsistent behavior; If we want that thenAccept always execute in different thread then the caller thread, 
or the task executor thread then we need to use different method.

#### thenAcceptAsync method of CompletableFuture
thenAcceptAsync method provides us consistent behavior to execute the Consumer in a different thread than the caller 
thread, or the task executor thread. Let's see this in example:
```text
 public static void main(String[] args) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Example3ThenAcceptAsyncCompletableFuture::supplierFunction);
        future.thenAcceptAsync(Example3ThenAcceptAsyncCompletableFuture::consumer);
        sleep(4000);
    }
```
We have used the same example above just modified the main method and called thenAcceptAsync method; which gives below
output:
```text
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
Printing the Number Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number is 5
```  
We can see both are executed in the CommonPool. To understand it better we can provide custom pool to both supplier
to execute the task and different custom pool to the consumer. Which looks like below:
```text
    public static void main(String[] args) {
        ForkJoinPool taskPool = new ForkJoinPool(10);
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Example3ThenAcceptAsyncCompletableFuture::supplierFunction,taskPool);
        ForkJoinPool consumerPool = new ForkJoinPool(10);
        future.thenAcceptAsync(Example3ThenAcceptAsyncCompletableFuture::consumer,consumerPool);
        sleep(4000);
    }
```
In above example we have provided two different pool to execute the actual task and execute Consumer task. Let's see the 
output of above:
```text
Generating Value Thread[ForkJoinPool-1-worker-9,5,main]
Printing the Number Thread[ForkJoinPool-2-worker-9,5,main]
Number is 5
```
We can see it executed in different pools.

There are several methods in CompletableFuture which provides default and Async methods both. They have same differences
as we have seen in thenAccept and thenAcceptAsync. 

Let's summarise the differences between Non-Async and Async method:

| Task Status      | Non-Async                |      Async       |
| ---------------- | -------------------------| ---------------- |
| Future Done      | Caller/Completing thread | Given Pool       |
| Future Not Done  | Completing Thread        | Given Pool       |

If no pool has been provided to Async method then they will execute in common ForkJoinPool.

#### thenApply or thenApplyAsync method of CompletableFuture
If we want to transform or do some other function before we consume the return of task completed then we use the 
thenApply or thenApplySync method. Let's say we want to double the number before we consume it. We can do like below:
```text
    public static void main(String[] args) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Example4ThenApplyCompletableFuture::supplierFunction);
        CompletableFuture<Integer> transformedFuture = future.thenApply(Example4ThenApplyCompletableFuture::transform);
        transformedFuture.thenAcceptAsync(Example4ThenApplyCompletableFuture::consumer);
        sleep(4000);
    }
```
As we can see we used the thenApply function and since result is available it will execute in the main thread, but we
used the thenAcceptAsync, so it will execute in a different thread in common pool. We can't change the sequence of 
registering the calls, because thenAccept method returns the CompletableFuture<Void> and thenApply take the Function 
which takes and an argument and returns a result. So if we interchange the call and Future is nothing to return it will
give compilation error. Let's see the output of above program:
```text
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
Doubling Number Thread[main,5,main]
Printing the Number Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number is 10
```
We can also chain the calls like below:
```text
    public static void main(String[] args) {
        CompletableFuture.supplyAsync(Example4ThenApplyCompletableFuture::supplierFunction)
                .thenApply(Example4ThenApplyCompletableFuture::transform)
                .thenAcceptAsync(Example4ThenApplyCompletableFuture::consumer);
        sleep(4000);
    }
```
#### thenRun method of CompletableFuture
Let's say after completion of all processes we want to log the result or notify someone using any mechanism; For which 
we can use the Runnable because we are not worrying about result after completion of task; that is just the effect we 
want to have to just provide notification to perform another task. In that case we use thenRun method which takes
runnable and Executes if the task has executed successfully.
```text
  public static void main(String[] args) {
        CompletableFuture.supplyAsync(Example4ThenApplyCompletableFuture::supplierFunction)
                .thenApply(Example4ThenApplyCompletableFuture::transform)
                .thenAcceptAsync(Example4ThenApplyCompletableFuture::consumer)
                .thenRun(()-> System.out.println("All Tasks Completed"));
        Example4ThenApplyCompletableFuture.sleep(4000);
    }
```
The output of above will be like:
```text
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
Doubling Number Thread[main,5,main]
Printing the Number Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number is 10
All Tasks Completed
```

#### completed method of CompletableFuture
Let's say we have one function which apply chain of operation on the CompletableFuture; this takes the CompletableFuture
to perform the task. Let's see below example:
```text
  public static void applyChainOfOperations(CompletableFuture<Integer> future) {
        System.out.println(future);
        future.thenApply(Example6CompleteCompletableFuture::transform)
                .thenAcceptAsync(Example6CompleteCompletableFuture::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));
    }

    public static void main(String[] args) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Example6CompleteCompletableFuture::supplierFunction);
        sleep(1000);
        applyChainOfOperations(future);
        CompletableFuture<Integer> test = new CompletableFuture<>();
        applyChainOfOperations(test);
        System.out.println(test);
        sleep(4000);
    }
```
In above example we are taking a Future in which we are passing the task to execute, and we are calling sleep on main
method so that result will be available, and then we are applying chain of operation. Then we created a CompletableFuture
object and pass it to applyChainOfOperations method. Let's see output of above code:
```text
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
java.util.concurrent.CompletableFuture@5fd0d5ae[Completed normally]
Doubling Number Thread[main,5,main]
Printing the Number Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number is 10
All Tasks Completed
java.util.concurrent.CompletableFuture@3feba861[Not completed]
java.util.concurrent.CompletableFuture@3feba861[Not completed, 1 dependents]
```
Here we can see that for first one the operation has executed and, also the status of the CompletableFuture is the 
"Completed normally". While for the second one no operation has executed and, we can see before passing to the function
the status was "Not completed" and after coming out of the function the status is "Not completed, 1 dependents". 

So what happen in above code for the second one. The chainOfOperations only apply on the CompletedFuture; since for the
first function the supplier has executed and CompletableFuture task has completed; and chainOfOperations apply on Future.
While for the second one CompletableFuture it never completed so status is NotCompleted before passing to the function
and when we pass it to the function pipeline build on the function; and then it is showing the status as the 
CompletableFuture is not completed on its completion 1 dependent is there.

If we want to pass a value manually and complete it; we will call completableFutureObject.complete(value). Lets see the
code for this:
```text
    public static void main(String[] args) {
        //CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Example6CompleteCompletableFuture::supplierFunction);
        //sleep(1000);
       // applyChainOfOperations(future);
        CompletableFuture<Integer> test = new CompletableFuture<>();
        applyChainOfOperations(test);
        System.out.println(test);
        test.complete(6);
        System.out.println(test);
        sleep(4000);
    }
```
The Output for above code is like below:c
```text
java.util.concurrent.CompletableFuture@14ae5a5[Not completed]
java.util.concurrent.CompletableFuture@14ae5a5[Not completed, 1 dependents]
Doubling Number Thread[main,5,main]
java.util.concurrent.CompletableFuture@14ae5a5[Completed normally]
Printing the Number Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number is 12
All Tasks Completed
```
After calling as we can see chain has executed and, also status updated to "Completed normally". It uses the value we
passed as the complete method argument to execute the further chain.

#### Cancelling the CompletableFuture
Cancelling a task in CompletableFuture is same as the Future. There is a cancel method; which takes a boolean argument
indicating that if the task is running we should interrupt or not; If passed true it will interrupt the thread if false
and task is running it will not cancel the task. There are some ground rules for cancelling the Future or 
CompletableFuture:
* A Cancelled CompletableFuture always return true for isDone or isCancelled method.
* A Completed CompletableFuture can't be cancelled as it is already completed.
* A Cancelled CompletableFuture can never be completed.
* A Completed CompletableFuture can't be complete again.
* A Cancelled CompletableFuture can't be cancel again.

#### Handling exception and failure of CompletableFuture
Till now, we have seen the happy path scenario of the task where CompletableFuture completes with data, and then we build
pipeline on it. We have changed our example a bit, In which we have created an instance variable in class which will 
complete the supplier function for the first time and fails at second time. Let's have a look on the supplier function 
modification:
```text
 private static int i = 1;

    public static int supplierFunction() {
        System.out.println("Generating Value " + Thread.currentThread());
        if (i % 2 == 0) {
            throw new RuntimeException("Something went wrong");
        }
        return i++;
    }
``` 
So when we call it second time it will below up with RunTimeException. Let's call this with task two times and see the 
result from the main
```text
    public static void main(String[] args) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Example7ExceptionCompletableFuture::supplierFunction);
        future.thenApply(Example7ExceptionCompletableFuture::transform)
                .thenAcceptAsync(Example7ExceptionCompletableFuture::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));
        System.out.println("----------------------------------------");
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(Example7ExceptionCompletableFuture::supplierFunction);
        future1.thenApply(Example7ExceptionCompletableFuture::transform)
                .thenAcceptAsync(Example7ExceptionCompletableFuture::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));

        sleep(5000);
    }
```
Output for above code is like below:
```text
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
Doubling Number Thread[main,5,main]
Printing the Number Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number is 2
All Tasks Completed
----------------------------------------
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
```
So we can see at second call it blow up; but we didn't get any information or any of transformation has applied. To
handle exception from the task or its transformation functions pipeline we chain exceptionally method where we catch the
exception and propagate it or return a value which later consumed by the transformation function, or I can say consumed
by the pipeline. Let's see example for this; We have created reportError Function and provide it as exceptionally function
which will be called only if the something went wrong while running the task; If you want to register same with any
transformation you need to chain it after that transformation. Code for the reportError and main is like below:
```text
 public static int reportError(Throwable throwable){
        throwable.printStackTrace();
        throw new RuntimeException(throwable.getMessage());
    }

    public static void main(String[] args) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Example7ExceptionCompletableFuture::supplierFunction);
        future.thenApply(Example7ExceptionCompletableFuture::transform)
                .thenAcceptAsync(Example7ExceptionCompletableFuture::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));
        System.out.println("----------------------------------------");
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(Example7ExceptionCompletableFuture::supplierFunction);
        future1.exceptionally(Example7ExceptionCompletableFuture::reportError)
                .thenApply(Example7ExceptionCompletableFuture::transform)
                .thenAcceptAsync(Example7ExceptionCompletableFuture::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));

        sleep(5000);
    }
```
So in report error we are taking exception printing stacktrace and again throwing the exception; but notice the return 
type of function is int, in case you want that after logging exception further pipeline should be executed with a 
default value then you can return from this function and further pipeline will be executed with this value. The output
of above code is like below:
```text
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
Doubling Number Thread[main,5,main]
Printing the Number Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number is 2
All Tasks Completed
----------------------------------------
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
java.util.concurrent.CompletionException: java.lang.RuntimeException: Something went wrong
	at java.util.concurrent.CompletableFuture.encodeThrowable(CompletableFuture.java:273)
	at java.util.concurrent.CompletableFuture.completeThrowable(CompletableFuture.java:280)
	at java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1606)
	at java.util.concurrent.CompletableFuture$AsyncSupply.exec(CompletableFuture.java:1596)
	at java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:289)
	at java.util.concurrent.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1056)
	at java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1692)
	at java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:157)
Caused by: java.lang.RuntimeException: Something went wrong
	at com.cwn.example.Example7ExceptionCompletableFuture.supplierFunction(Example7ExceptionCompletableFuture.java:12)
	at java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1604)
	... 5 more
```
 We have printed the stacktrace in the reportError method and again thrown the exception. Let's try by returning a value:
 ```text
 public static int reportError(Throwable throwable){
        throwable.printStackTrace();
       //throw new RuntimeException(throwable.getMessage());
        return 10;
    }
```
The output after above change is like below:
```text
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
Doubling Number 1 Thread[main,5,main]
Printing the Number Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number is 2
All Tasks Completed
----------------------------------------
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
Doubling Number 10 Thread[main,5,main]
Printing the Number Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number is 20
All Tasks Completed
java.util.concurrent.CompletionException: java.lang.RuntimeException: Something went wrong
	at java.util.concurrent.CompletableFuture.encodeThrowable(CompletableFuture.java:273)
	at java.util.concurrent.CompletableFuture.completeThrowable(CompletableFuture.java:280)
	at java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1606)
	at java.util.concurrent.CompletableFuture$AsyncSupply.exec(CompletableFuture.java:1596)
	at java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:289)
	at java.util.concurrent.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1056)
	at java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1692)
	at java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:157)
Caused by: java.lang.RuntimeException: Something went wrong
	at com.cwn.example.Example7ExceptionCompletableFuture.supplierFunction(Example7ExceptionCompletableFuture.java:12)
	at java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1604)
	... 5 more
```
In this example after printing the stacktrace the pipeline continues with the value returned by the function which is 10.
Let's try to throw the exception from the transformation function if number is greater than 9.
```text
    public static int transform(int number) {
        System.out.println("Doubling Number " + number + " " + Thread.currentThread());
        if(number>9){
            throw new RuntimeException("Number is greater than 9; can't process");
        }
        return number * 2;
    }

    public static int reportError(Throwable throwable) {
        throwable.printStackTrace();
       // throw new RuntimeException(throwable.getMessage());
       return 10;
    }
```
The output of the after change is like below:
```text
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
Doubling Number 1 Thread[main,5,main]
Printing the Number Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number is 2
All Tasks Completed
----------------------------------------
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
Doubling Number 10 Thread[main,5,main]
java.util.concurrent.CompletionException: java.lang.RuntimeException: Something went wrong
	at java.util.concurrent.CompletableFuture.encodeThrowable(CompletableFuture.java:273)
	at java.util.concurrent.CompletableFuture.completeThrowable(CompletableFuture.java:280)
	at java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1606)
	at java.util.concurrent.CompletableFuture$AsyncSupply.exec(CompletableFuture.java:1596)
	at java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:289)
	at java.util.concurrent.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1056)
	at java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1692)
	at java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:157)
Caused by: java.lang.RuntimeException: Something went wrong
	at com.cwn.example.Example7ExceptionCompletableFuture.supplierFunction(Example7ExceptionCompletableFuture.java:12)
	at java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1604)
	... 5 more
```
Since the transform throws exception further pipeline has not executed; But We can't see our exception in the log. This 
is because we didn't register the exceptionally after transform function, so it does not have any track for what to 
do when something went wrong in  the transform function. Let's create a function and then register it with the transform
exceptionally chain.
```text
    public static int reportTransformError(Throwable throwable) {
        throwable.printStackTrace();
        throw new RuntimeException("Something went wrong");
    }

    public static int chainExceptions(Throwable throwable) {
        System.out.println("Chained messages is " + throwable.getMessage());
        throw new RuntimeException("Something went wrong");
    }

 public static void main(String[] args) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Example7ExceptionCompletableFuture::supplierFunction);
        future.thenApply(Example7ExceptionCompletableFuture::transform)
                .thenAcceptAsync(Example7ExceptionCompletableFuture::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));
        System.out.println("----------------------------------------");
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(Example7ExceptionCompletableFuture::supplierFunction);
        future1.exceptionally(Example7ExceptionCompletableFuture::reportError)
                .thenApply(Example7ExceptionCompletableFuture::transform)
                .exceptionally(Example7ExceptionCompletableFuture::reportTransformError)
                .exceptionally(Example7ExceptionCompletableFuture::chainExceptions)
                .thenAcceptAsync(Example7ExceptionCompletableFuture::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));

        sleep(5000);
    }
```

In above code we have registered the exceptionally with transform function also chained the exceptions as well. Let's see
output of above code:
```text
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
Doubling Number 1 Thread[main,5,main]
Printing the Number Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number is 2
All Tasks Completed
----------------------------------------
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
Doubling Number 10 Thread[main,5,main]
Chained messages is java.lang.RuntimeException: Something went wrong
java.util.concurrent.CompletionException: java.lang.RuntimeException: Number is greater than 9; can't process
	at java.util.concurrent.CompletableFuture.encodeThrowable(CompletableFuture.java:273)
	at java.util.concurrent.CompletableFuture.completeThrowable(CompletableFuture.java:280)
	at java.util.concurrent.CompletableFuture.uniApply(CompletableFuture.java:618)
	at java.util.concurrent.CompletableFuture.uniApplyStage(CompletableFuture.java:628)
	at java.util.concurrent.CompletableFuture.thenApply(CompletableFuture.java:1996)
	at com.cwn.example.Example7ExceptionCompletableFuture.main(Example7ExceptionCompletableFuture.java:54)
Caused by: java.lang.RuntimeException: Number is greater than 9; can't process
	at com.cwn.example.Example7ExceptionCompletableFuture.transform(Example7ExceptionCompletableFuture.java:25)
	at java.util.concurrent.CompletableFuture.uniApply(CompletableFuture.java:616)
	... 3 more
```

We can see the stack trace has printed for the transform function  also chained message for exception has printed.


With all above example we can conclude that the Data Track and Error track can get  switched in CompletableFuture 
whenever Data has generated from the Error Track the track switch and pipeline for Data Track get start executing
and whenever any error occurred in Data Track pipeline it switch back to Error Track pipeline and Error Track get
start executing.

Let's talk about CompletableFuture state when exception occurred and how we can complete a function by calling method.
To check status of each task we have created a method which checks task is done or not, is it done due to cancellation, 
or it is completed due to exception.
```text
    public static void printStateOfFuture(CompletableFuture<Integer> completableFuture) {
        System.out.println("Is task Completed               : " + completableFuture.isDone());
        System.out.println("Is task Cancelled               : " + completableFuture.isCancelled());
        System.out.println("Is task Completed Exceptionally : " + completableFuture.isCompletedExceptionally());
    }

    public static void main(String[] args) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Example8ExceptionCompletableFutureState::supplierFunction);
        future.thenApply(Example8ExceptionCompletableFutureState::transform)
                .thenAcceptAsync(Example8ExceptionCompletableFutureState::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));
        printStateOfFuture(future);
        System.out.println("----------------------------------------");
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(Example8ExceptionCompletableFutureState::supplierFunction);
        future1.exceptionally(Example8ExceptionCompletableFutureState::reportError)
                .thenApply(Example8ExceptionCompletableFutureState::transform)
                .thenAcceptAsync(Example8ExceptionCompletableFutureState::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));
        printStateOfFuture(future1);
        System.out.println("----------------------------------------");
        CompletableFuture<Integer> future2 = new CompletableFuture<>();
        future2.exceptionally(Example8ExceptionCompletableFutureState::reportError)
                .thenApply(Example8ExceptionCompletableFutureState::transform)
                .thenAcceptAsync(Example8ExceptionCompletableFutureState::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));
        future2.completeExceptionally(new RuntimeException("Task Can't performed; due to some problem"));
        printStateOfFuture(future2);
        System.out.println("----------------------------------------");
        CompletableFuture<Integer> future3 = new CompletableFuture<>();
        future3.exceptionally(Example8ExceptionCompletableFutureState::reportError)
                .thenApply(Example8ExceptionCompletableFutureState::transform)
                .thenAcceptAsync(Example8ExceptionCompletableFutureState::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));
        future3.cancel(true);
        printStateOfFuture(future3);
        sleep(5000);
    }
```

The output of above code is: 
```text
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
Doubling Number 1 Thread[main,5,main]
Printing the Number Thread[ForkJoinPool.commonPool-worker-9,5,main]
Number is 2
All Tasks Completed
Is task Completed               : true
Is task Cancelled               : false
Is task Completed Exceptionally : false
----------------------------------------
Generating Value Thread[ForkJoinPool.commonPool-worker-9,5,main]
Exception happened with message java.lang.RuntimeException: Something went wrong
Is task Completed               : true
Is task Cancelled               : false
Is task Completed Exceptionally : true
----------------------------------------
Exception happened with message Task Can't performed; due to some problem
Is task Completed               : true
Is task Cancelled               : false
Is task Completed Exceptionally : true
----------------------------------------
Exception happened with message null
Is task Completed               : true
Is task Cancelled               : true
Is task Completed Exceptionally : true
```

Task 1 completed successfully and had no exception so task has completed and neither cancelled nor completed 
exceptionally as we can see in output. Task 2 had completed with exception so task has completed and completed with 
exception  but not cancelled.I n Task3 we have completedExceptionally the task by providing custom exception and message
which we can see as the report error; So it's status is same like Task 2. In Task 4 we perform the cancel operation 
since there which marks all as true; Because method cancel has the same effect as 
completeExceptionally(new CancellationException()).

### Working with multiple CompletableFuture
We have seen how to run a task and handling failures in CompletableFuture. From this section on-wards we will see how
to work with multiple CompletableFuture for few situations which we may face while development. Let's get started.

#### thenCombine method
This method helps to combine the results of two different CompletableFuture results and apply pipeline on it if required.
For this we are calling an API which provides the currency exchange from one to other. The Logic for that is in 
class [CurrencyConverter](src/main/java/com/cwn/example/CurrencyConverter.java). 

Let's create two Completable Future and then try to consume results for getting desired output. In this example we will
get the current currency conversion rate from USD to INR and GBP to INR and sum it up and consume the output to print 
on console.
```text
public static void main(String[] args) {
        CompletableFuture<Double> task1 = CompletableFuture.supplyAsync(
                () -> converter.convertCurrency("USD", "INR"));
        CompletableFuture<Double> task2 = CompletableFuture.supplyAsync(
                () -> converter.convertCurrency("GBP", "INR"));

        task1.thenCombine(task2, (usdToInr, gbpToInr) -> usdToInr + gbpToInr)
                .thenAcceptAsync(System.out::println);
        sleep(5000);
    }
```

The output of above code is the sum of the conversion rate from USD and GBP to INR. The above code is just running the
task and getting conversion rate and summing them; let's try to apply the transform on individual function to multiply 
with desired value.
```text
    public static void main(String[] args) {
        int usdQuantity = 5;
        int gbpQuantity = 10;
        CompletableFuture<Double> task1 = CompletableFuture
                .supplyAsync(() -> converter.convertCurrency("USD", "INR"))
                .thenApply(value -> usdQuantity * value);
        CompletableFuture<Double> task2 = CompletableFuture
                .supplyAsync(() -> converter.convertCurrency("GBP", "INR"))
                .thenApply(value -> gbpQuantity * value);

        task1.thenCombine(task2, (usdToInr, gbpToInr) -> usdToInr + gbpToInr)
                .thenAcceptAsync(System.out::println);
        sleep(5000);
    }
```
In above code we are performing lot of ceremony also we had applied the transformation on individual results; Lets
remove the ceremony and apply pipeline over the combine results.
```text
    public static void main(String[] args) {
        int usdQuantity = 5;
        int gbpQuantity = 10;
        CompletableFuture
                .supplyAsync(() -> converter.convertCurrency("USD", "INR"))
                .thenApply(value -> usdQuantity * value)
                .thenCombine(CompletableFuture
                                .supplyAsync(() -> converter.convertCurrency("GBP", "INR"))
                                .thenApply(value -> gbpQuantity * value),
                        Double::sum)
                .thenApply(value -> "INR of " + usdQuantity + " USD and " + gbpQuantity + " GPB " + gbpQuantity + "is : " + value)
                .thenAccept(System.out::println);

        sleep(5000);
    }
```
The output of above code is:
```
INR of 5 USD and 10 GPB 10is : 1342.4399653205296
```

#### thenCompose method
In our previous example we are returning the value after completing the task; Just consider if we are returning the
CompletableFuture, and then we are consuming the result. Typical this will looks like below:
```text
 public static CompletableFuture<String> getCurrencySymbol(int index) {
        String[] symbols = {"USD", "GBP", "JPY"};
        return CompletableFuture.supplyAsync(() -> symbols[index]);
    }

    public static CompletableFuture<Double> getConversionRateInInr(String symbol, int amount) {
        return CompletableFuture.supplyAsync(() -> currencyConverter.convertCurrency(symbol, "INR"))
                .thenApply(value -> value * amount);
    }

    public static void main(String[] args) {
      getConversionRateInInr("USD",50).thenAccept(System.out::println);
      sleep(2000);
    }
``` 

In above function we have declared two function one is returning the symbol and other is coverting the amount to INR.
In above example we right now call the converting function directly; But what if the main will look like below, where
we take the symbol from getSymbol function and try to apply the conversion rate. 
```text
 public static void main(String[] args) {
        getCurrencySymbol(1)
                .thenApply(symbol -> getConversionRateInInr(symbol,50))
                .thenAccept(System.out::println);
        sleep(2000);
    }
```
But this will not print the result instead it will return the CompletableFuture; because return type of the convert
function is CompletableFuture. So It will further go in chain and print CompletableFuture; But we need to print the 
calculated result after getting the value from convert function; For that we will replace the thenApply call to 
thenCompose method which, will get the value from the convert function and forward the value to further pipeline.
Let's see code for this:
```text
 public static void main(String[] args) {
        getCurrencySymbol(0)
                .thenCompose(symbol -> getConversionRateInInr(symbol,50))
                .thenAccept(System.out::println);
        sleep(2000);
    }
```

#### acceptEither and applyToEither method
When we have multiple CompletableFuture, and we want to perform the operation on any of them is complete and by taking
the value whichever completed first; so we use acceptEither. For example:
```text
    public static CompletableFuture<String> getCurrencyExchangeRate(String from, String to, int amount) {
        return CompletableFuture.supplyAsync(() -> currencyConverter.convertCurrency(from, to))
                .thenApply(value -> from + " to " + to + " of amount " + amount + " is " + (value * amount));
    }

    public static void main(String[] args) {
        IntStream.rangeClosed(1, 10).forEach(i ->
                getCurrencyExchangeRate("USD", "INR", 50)
                        .acceptEither(getCurrencyExchangeRate("GBP", "INR", 50), System.out::println));
        sleep(10000);
    }
```

here we are running the example ten times using loop, and then we are trying to get converted rate for USD to INR in
one CompletableFuture and GBP to INR in another one we have provided in acceptEither. So whichever of them complete first
it will perform the opertion provided as Consumer in acceptEither second argument for our case printing the value. 
Output for this may vary; But one of my run output is like below:
```text
USD to INR of amount 50 is 3662.83500837521
USD to INR of amount 50 is 3662.83500837521
USD to INR of amount 50 is 3662.83500837521
GBP to INR of amount 50 is 4880.782322415044
GBP to INR of amount 50 is 4880.782322415044
GBP to INR of amount 50 is 4880.782322415044
USD to INR of amount 50 is 3662.83500837521
GBP to INR of amount 50 is 4880.782322415044
USD to INR of amount 50 is 3662.83500837521
USD to INR of amount 50 is 3662.83500837521
```

So whichever completes first it will print result fot that CompletableFuture; Same as with applyToEither; Which apply
the function whichever CompletableFuture completes first. Let's take below example:

```text
  public static void main(String[] args) {
      /*  IntStream.rangeClosed(1, 10).forEach(i ->
                getCurrencyExchangeRate("USD", "INR", 50)
                        .acceptEither(getCurrencyExchangeRate("GBP", "INR", 50), System.out::println));*/
        IntStream.rangeClosed(1, 10).forEach(i ->
                getCurrencyExchangeRate("USD", "INR", 50)
                        .applyToEither(getCurrencyExchangeRate("GBP", "INR", 50),
                                value -> value + " Completed")
                        .thenAccept(System.out::println));
        sleep(10000);
    }
```

#### runAfter methods
There are several cases where we want to perform some action when one of two CompletableFuture completes or both
of them Completes. Let's see an example for same case:
```text
   public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            CompletableFuture<String> task1 = getCurrencyExchangeRate("USD", "INR", 51);
            CompletableFuture<String> task2 = getCurrencyExchangeRate("GBP", "INR", 50);
            task1.runAfterEither(task2, () -> System.out.println("One Task Completed"));
        }
        sleep(5000);
    }
```
The output of this is 10 times printed "One Task Completed", as soon as One of the task completed the function will run.
Let's see what happens if any of task fail. To simulate this we added a condition in converter class that it will throw
an exception after wait of few second if the from symbol is JPY.
```text

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            CompletableFuture<String> task1 = getCurrencyExchangeRate("USD", "INR", 51);
            CompletableFuture<String> task2 = getCurrencyExchangeRate("JPY", "INR", 50);
            task1.runAfterEither(task2, () -> System.out.println("One Task Completed"));
        }
        sleep(5000);
    }
```
The output of above is not predicted but for one of my run the output is like below:
```text
One Task Completed
One Task Completed
One Task Completed
One Task Completed
```
Why this is printed 4 times; So as we discussed whatever task completed trigger runAfterEither but since our task
is completedExceptionally that's why function didn't run. If we register exceptionally we can see this. Let's change it
like below:
```text
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            CompletableFuture<String> task1 = getCurrencyExchangeRate("USD", "INR", 51);
            CompletableFuture<String> task2 = getCurrencyExchangeRate("JPY", "INR", 50);
            task1.runAfterEither(task2, () -> System.out.println("One Task Completed"))
                    .exceptionally(Example12RunAfterCompletableFuture::reportIt);
        }
        sleep(5000);
    }

    private static Void reportIt(Throwable throwable) {
        System.out.println(throwable.getMessage());
        throw new RuntimeException(throwable.getMessage());
    }
``` 
Below is output for the program:
```text
java.lang.RuntimeException: JPY currency can't be converted
java.lang.RuntimeException: JPY currency can't be converted
java.lang.RuntimeException: JPY currency can't be converted
java.lang.RuntimeException: JPY currency can't be converted
One Task Completed
One Task Completed
One Task Completed
One Task Completed
One Task Completed
One Task Completed
```
So when sleep is over for JPY call then it throws the exception then exceptionally has run and when USD task has completed
then it will run the actual consumer. So, If Either of task has completed without any exception it will run the Consumer
block otherwise exceptionally block if registered.

Let's see runAfterBoth:
```text
 public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            CompletableFuture<String> task1 = getCurrencyExchangeRate("USD", "INR", 51);
            CompletableFuture<String> task2 = getCurrencyExchangeRate("GBP", "INR", 50);
            task1.runAfterBoth(task2, () -> System.out.println("Both Tasks Completed"))
                    .exceptionally(Example12RunAfterCompletableFuture::reportIt);
        }
        sleep(5000);
    }
```
For above program ten times "Both Tasks Completed"; Because each task will complete successfully without exception. 
Let's see what happens if any of task fails:
```text
  for (int i = 0; i < 10; i++) {
            CompletableFuture<String> task1 = getCurrencyExchangeRate("USD", "INR", 51);
            CompletableFuture<String> task2 = getCurrencyExchangeRate("JPY", "INR", 50);
            task1.runAfterBoth(task2, () -> System.out.println("Both Tasks Completed"))
                    .exceptionally(Example12RunAfterCompletableFuture::reportIt);
        }
```
All ten times exceptionally will run because JPY call will fail and in case of exception exceptionally will be executed.

#### anyOf method
Till now, we have seen one or two CompletableFuture operations; Now we will see with more than two CompletableFuture
operations. If we want to perform the operation on the pipeline if any of task completed then we use anyOf method and 
append the pipeline. Let's see this by below example and it's Output:
```
  public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            CompletableFuture<String> task1 = getCurrencyExchangeRate("CAD", "INR", 50);
            CompletableFuture<String> task2 = getCurrencyExchangeRate("CAD", "INR", 60);
            CompletableFuture<String> task3 = getCurrencyExchangeRate("CAD", "INR", 70);
            CompletableFuture<String> task4 = getCurrencyExchangeRate("CAD", "INR", 80);
            CompletableFuture<String> task5 = getCurrencyExchangeRate("CAD", "INR", 90);
            CompletableFuture<String> task6 = getCurrencyExchangeRate("CAD", "INR", 100);
            CompletableFuture<Integer> task7 = getCurrencyExchangeRate("CAD", "INR", 100)
                    .thenApply(value -> 5);
            CompletableFuture.anyOf(task1, task2, task3, task4, task5, task6, task7)
                    .thenAccept(System.out::println);
        }
        sleep(7000);
    }
```
Output of the program is:
```text
CAD to INR of amount 60 is 3363.957438625729
CAD to INR of amount 80 is 4485.276584834306
CAD to INR of amount 50 is 2803.297865521441
CAD to INR of amount 90 is 5045.936157938594
CAD to INR of amount 50 is 2803.297865521441
5
CAD to INR of amount 100 is 5606.595731042882
CAD to INR of amount 50 is 2803.297865521441
5
CAD to INR of amount 50 is 2803.297865521441
```
What is we try to apply transform function:
```text
 CompletableFuture.anyOf(task1, task2, task3, task4, task5, task6, task7)
                    .thenApply(value -> value * 2)
                    .thenAccept(System.out::println);
```
If we try to use above then we get the compile time error which is like below:
```text
java: bad operand types for binary operator '*'
  first type:  java.lang.Object
  second type: int
```
which indicates the anyOf method return Object type; and anyOf can take any type of CompletableFuture. If we want to 
apply transformation we can do like below:
```text
 CompletableFuture.anyOf(task1, task2, task3, task4, task5, task6, task7)
                    .thenApply(value -> {
                        if (value instanceof Integer) {
                            return (int) value * 2;
                        }
                        return value;
                    })
                    .thenAccept(System.out::println);
```
So whenever the first task completed has integer value it will double it and print it; If it has other than integer then
it will simple return the value and print it. If any of the task completed exceptionally i.e. fails then same rules
are applied to anyOf as was in acceptEither. If the first task completed exceptionally then exceptionally will run if 
registers; If the first task has completed with data actual pipeline will be executed with that data.

#### timeout In case of multiple CompletableFuture
Since we are operating with multiple CompletableFuture; and just consider the scenerio where making a call or getting 
resource is taking time; Then in that case we don't want to stuck in anyOf call and their should be a timeout. To perform
the timeout right now no available method are available; So we will create a task with sleep which will sleep for
timeout seconds and whenever timeout happens it throws the RunTimeExecption for timeout. Let's see code for this:
```text
public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            CompletableFuture<String> task1 = getCurrencyExchangeRate("CAD", "INR", 50);
            CompletableFuture<String> task2 = getCurrencyExchangeRate("CAD", "INR", 60);
            CompletableFuture<String> task3 = getCurrencyExchangeRate("CAD", "INR", 70);
            CompletableFuture<String> task4 = getCurrencyExchangeRate("CAD", "INR", 80);
            CompletableFuture<String> task5 = getCurrencyExchangeRate("CAD", "INR", 90);
            CompletableFuture<String> task6 = getCurrencyExchangeRate("CAD", "INR", 100);
            CompletableFuture<Integer> task7 = getCurrencyExchangeRate("CAD", "INR", 100)
                    .thenApply(value -> 5);
            CompletableFuture<Void> timeoutTask = CompletableFuture.supplyAsync(() -> {
                sleep(1000);
                throw new RuntimeException("Timeout Happened");
            });
            CompletableFuture.anyOf(task1, task2, task3, task4, task5, task6, task7,timeoutTask)
                    .thenApply(value -> {
                        if (value instanceof Integer) {
                            return (int) value * 2;
                        }
                        return value;
                    })
                    .thenAccept(System.out::println);
        }
        sleep(7000);
    }
```
So if None of the task completed within 1000 milliseconds then it will come out of sleep and complete exceptionally.

#### allOf method
When we want to run an operation after completing of all the tasks then we use the allOf Method. Let's see an example
of this:
```
public static void main(String[] args) {
        CompletableFuture<String> task1 = getCurrencyExchangeRate("CAD", "INR", 50);
        CompletableFuture<String> task2 = getCurrencyExchangeRate("CAD", "INR", 60);
        CompletableFuture<String> task3 = getCurrencyExchangeRate("CAD", "INR", 70);
        CompletableFuture<String> task4 = getCurrencyExchangeRate("CAD", "INR", 80);
        CompletableFuture<String> task5 = getCurrencyExchangeRate("CAD", "INR", 90);
        CompletableFuture<String> task6 = getCurrencyExchangeRate("CAD", "INR", 100);
        CompletableFuture<Integer> task7 = getCurrencyExchangeRate("CAD", "INR", 100)
                .thenApply(value -> 5);
        CompletableFuture.allOf(task1, task2, task3, task4, task5, task6, task7)
                .thenRun(() -> System.out.println("All Tasks are completed"));
        sleep(7000);
    }
```
So when all task will completed successfully then it prints "All Tasks are completed". If we change one task to JPY
then it will fail; and if register the exceptionally clause then it will execute that block.

### Java 9 timeout Method
Till Java 8; CompletableFuture does not have any timeout method and, it waits for the CompletableFuture to complete or
complete exceptionally. Let's take an example of Java 8 CompletableFuture first:
```text
    private static void java8CompletableFuture() {
        CompletableFuture<Double> converterFuture =
                CompletableFuture.supplyAsync(()-> converter.convertCurrency("USD","INR"));
        converterFuture.thenAccept(System.out::println);
        
        CompletableFuture<Double> converterFutureJpy =
                CompletableFuture.supplyAsync(()-> converter.convertCurrency("JPY","INR"));
        converterFutureJpy.exceptionally(TimeOutExample::reportError).thenAccept(System.out::println);
    }

     public static double reportError(Throwable throwable){
            System.out.println(throwable);
            throw new RuntimeException(throwable.getMessage());
      }

  public static void main(String[] args) throws InterruptedException {
        java8CompletableFuture();
        sleep(5000);
    }
```

The above code will generate below output:
```text
java.util.concurrent.CompletionException: java.lang.RuntimeException: JPY currency can't be converted
73.45103179972938
```

And If main ends before the CompletableFuture completes then there will be no output. If we comment the sleep line in 
main method, then there will be no output and, we don't even get any exception that CompletableFuture not completed
within time. 

In Java 9, In CompletableFuture they have added timeout method, so If any CompletableFuture didn't complete within the
time provided, It will complete with a default value if we use completeOnTimeout, and with TimeoutException if we use
orTimeout method of Java 9. Let's see an Example for both:
```text
    private static void java9CompletableFuture() {
        CompletableFuture<Double> completeTimeout =
                CompletableFuture.supplyAsync(()-> converter.convertCurrency("USD","INR"));
        completeTimeout.thenAccept(System.out::println);
        completeTimeout.completeOnTimeout(50.0,2, TimeUnit.SECONDS);

        CompletableFuture<Double>  orTimeout=
                CompletableFuture.supplyAsync(()-> converter.convertCurrency("USD","INR"));
        orTimeout.exceptionally(TimeOutExample::reportError).thenAccept(System.out::println);
        orTimeout.orTimeout(2, TimeUnit.SECONDS);

        CompletableFuture<Double> converterFutureJpy =
                CompletableFuture.supplyAsync(()-> converter.convertCurrency("JPY","INR"));
        converterFutureJpy.exceptionally(TimeOutExample::reportError).thenAccept(System.out::println);
    }
```
Above output will for above code will be below:
```text
50.0
java.util.concurrent.TimeoutException
java.util.concurrent.CompletionException: java.lang.RuntimeException: JPY currency can't be converted
```
Since the first one has not completed within the time and, we have given the default value 50.0; and that we get in the
output. In Second one we have given if task not completes it will complete with an TimeoutException; And Third one will 
the same as it is because we didn't have registered the timeout with this.





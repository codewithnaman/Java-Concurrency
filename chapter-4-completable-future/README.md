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
The Output for above code is like below:
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

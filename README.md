## 简介
task pipeline 任务管道框架，允许用户自定义key，拥有相同的key的任务按照提交的顺序串行执行，不同key的任务按照提交顺序并行执行。

## 适用场景
举一个kafka消费消息的例子。我们经常使用kafka的partition顺序消费来保证消息消费的有序性。而串行消费，则可能会出现消息积压的情况。使用这个框架可以保证顺序的同时，利用多线程并行的能力，缓解消息积压的情况，提升系统的性能。

## 任务提交
TaskPipelineManager暴露了两个提交任务的方法，submitUninterruptedPipelineTask方法保证当任务管道中的任务节点出错时，管道不中断，继续执行。submitInterruptedPipelineTask方法保证当任务管道中的任务节点出错时，管道中断，终止执行。

## 任务视图
任务视图是实时查看任务数量，评估任务压力的接口，可结合用户的限流策略进行使用。TaskPipelineManager暴露了三个查看任务视图的方法，TaskPipelineManager#getTaskView()是用来查看总任务情况，TaskPipelineManager#getTaskView(String taskNamespace)是用来查看taskNamespace的任务情况，TaskPipelineManager#getTaskView(String taskNamespace, String pipeline)是用来查看pipeline的任务情况.

## 高性能
和传统线程池提交任务的流程不同，具有相同taskNamespace下的相同pipelineId使用同一个线程进行执行，降低了线程创建，销毁，切换的开销。


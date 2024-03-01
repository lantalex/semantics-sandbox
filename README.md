# Нестандартные расширения модели памяти на практике

Примеры для доклада на [SnowOne 2024](https://snowone.ru/2024/speakers/alantsov)
и [JPoint 2024](https://jpoint.ru/talks/8f68169b548b4e29b2caeb664c05ef66/)

> Продолжение доклада с JPoint
> 2023 [«Не happens-before единым: нестандартные семантики»](https://www.youtube.com/watch?v=UZbPOtEgcoY).
>
>В первой части мы познакомились с существующими в Java семантиками и теми гарантиями, что они предоставляют. Теперь
> время узнать, как эти семантики используются на практике. Рассмотрим практический аспект применения семантик на
> реальных
> примерах, дойдем до нашумевшей в свое время библиотеки Disruptor и даже узнаем, как написать свою очередь — еще более
> производительную в некоторых сценариях.
>
>Доклад будет интересен всем, кто интересуется многопоточным программированием, моделями памяти и популярными lock-free
> библиотеками.
>
>

## Примеры

| Номера слайдов | Файл                                                                                                                 | Комментарий                                                                                                                                                    |
|:--------------:|----------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
|      6-10      | [`VolatileSF.java`](./src/jcstress/java/io/github/lantalex/singleton/VolatileSF.java)                                | Классическая корректная реализация Double-Checked Locking Singleton с помощью _volatile_                                                                       |
|     12-14      | [`CompletelyBrokenSF.java`](./src/jcstress/java/io/github/lantalex/singleton/CompletelyBrokenSF.java)                | Классическая некорректная реализация Double-Checked Locking Singleton без использования _volatile_                                                             |
|       15       | [`PlainSemantic.java`](./src/jcstress/java/io/github/lantalex/coherence/PlainSemantic.java)                          | Отсуствие гарантии когерентности для plain-семантики. Реальный тест отличается от приведенного на слайде ввиду сложности получения воспроизводимого результата |
|       19       | [`OpaqueSemantic.java`](./src/jcstress/java/io/github/lantalex/coherence/OpaqueSemantic.java)                        | Гарантия когерентности для opaque-семантики.                                                                                                                   |
|     20-22      | [`BrokenSF_WithCoherence.java`](./src/jcstress/java/io/github/lantalex/singleton/BrokenSF_WithCoherence.java)        | Все еще некорректный Double-Checked Locking Singleton с исправленной проблемой когерентности. Проблема с причинностью все еще присутствует.                    |
|     23-32      | [`RelaxedSF.java`](./src/jcstress/java/io/github/lantalex/singleton/RelaxedSF.java)                                  | Исправление Double-Checked Locking Singleton с помощью acquire-release семантики                                                                               |
|     33-39      | [`RelaxedSF_FinalVersion.java`](./src/jcstress/java/io/github/lantalex/singleton/RelaxedSF_FinalVersion.java)        | Финальная версия Double-Checked Locking Singleton на ослабленных семантиках                                                                                    |
|     53-67      | [`SPSC_VolatileQueue.java`](./src/main/java/io/github/lantalex/queue/spsc/basic/SPSC_VolatileQueue.java)             | Базовая реализация SPSC-очереди на volatile-семантике                                                                                                          |
|     68-82      | [`SPSC_AcqRelQueue.java`](./src/main/java/io/github/lantalex/queue/spsc/basic/SPSC_AcqRelQueue.java)                 | Базовая реализация SPSC-очереди на acquire-release семантике                                                                                                   |
|     83-88      | [`BasicQueueBenchmark.java`](./src/jmh/java/io/github/lantalex/BasicQueueBenchmark.java)                             | Бенчмарк базовых реализаций SPSC-очередей                                                                                                                      |
|     89-107     | [`SPSC_ImprovedQueue.java`](./src/main/java/io/github/lantalex/queue/spsc/improved/SPSC_ImprovedQueue.java)          | Улучшенная реализация SPSC-очередей с идеей использования локальных копий индексов                                                                             |
|    108-110     | [`ImprovedQueueBenchmark.java`](./src/jmh/java/io/github/lantalex/ImprovedQueueBenchmark.java)                       | Бенчмарк улучшенных реализаций SPSC-очередей                                                                                                                   |
|    108-110     | [`ImprovedQueueBenchmark.java`](./src/jmh/java/io/github/lantalex/ImprovedQueueBenchmark.java)                       | Бенчмарк улучшенных реализаций SPSC-очередей                                                                                                                   |
|      111       | [`SPSC_DisruptedQueue.java`](./src/main/java/io/github/lantalex/queue/spsc/SPSC_DisruptedQueue.java)                 | Улучшенная реализация SPSC-очереди с переиспользованием объектов                                                                                               |
|    112-113     | [`DisruptorBenchmark.java`](./src/jmh/java/io/github/lantalex/DisruptorBenchmark.java)                               | Бенчмарк Disruptor (Single Consumer)                                                                                                                           |
|      114       | [`SPSC_DisruptedQueue_Benchmark.java`](./src/jmh/java/io/github/lantalex/SPSC_DisruptedQueue_Benchmark.java)         | Бенчмарк улучшенной реализация SPSC-очереди с переиспользованием объектов                                                                                      |
|    116-135     | [`SimpleSeqlockTest.java`](./src/jcstress/java/io/github/lantalex/seqlock/SimpleSeqlockTest.java)                    | Seqlock-версия MyObject + тест                                                                                                                                 |
|    136-149     | [`StampedQueueForMyObject.java`](./src/main/java/io/github/lantalex/queue/spsc/seqlock/StampedQueueForMyObject.java) | SPSC-очередь, использующая seqlock                                                                                                                             |
|    150-151     | [`StampedQueueIdleBenchmark.java`](./src/jmh/java/io/github/lantalex/StampedQueueIdleBenchmark.java)                 | Бенчмарк SPSC-очереди, использующей seqlock                                                                                                                    |

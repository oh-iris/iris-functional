/*
 * copyright(c) 2018-2022 tabuyos all right reserved.
 */
package com.hantasmate.iris;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Functor
 *
 * @author tabuyos
 * @since 2022/12/26
 */
@SuppressWarnings("unused")
public class Functor<T> {

  private static final Functor<?> EMPTY = new Functor<>(null);
  private Predicate<? super T> validator;
  private final T value;

  private Functor(T value) {
    this.value = value;
  }

  @SuppressWarnings("unchecked")
  public static <R> Functor<R> empty() {
    return (Functor<R>) EMPTY;
  }

  /**
   * Pointed Functor
   *
   * @param value value
   * @param <R> R
   * @return new Functor
   */
  public static <R> Functor<R> of(R value) {
    Objects.requireNonNull(value);
    return new Functor<>(value);
  }

  /**
   * Maybe Functor
   *
   * @param value value
   * @param <R> R
   * @return new Functor
   */
  public static <R> Functor<R> maybe(R value) {
    return value == null ? empty() : of(value);
  }

  /**
   * If a value is present, returns {@code true}, otherwise {@code false}.
   *
   * @return {@code true} if a value is present, otherwise {@code false}
   */
  public boolean isPresent() {
    return value != null;
  }

  /**
   * If a value is not present, returns {@code true}, otherwise {@code false}.
   *
   * @return {@code true} if a value is not present, otherwise {@code false}
   * @since 11
   */
  public boolean isEmpty() {
    return value == null;
  }

  /**
   * Functor
   *
   * @param fn function
   * @param <R> R
   * @return Functor
   */
  public <R> Functor<R> map(Function<? super T, ? extends R> fn) {
    return isEmpty() ? empty() : maybe(fn.apply(this.value));
  }

  /**
   * Monad Functor
   *
   * @param fn function
   * @param <R> R
   * @return Functor
   */
  @SuppressWarnings("unchecked")
  public <R> Functor<R> flatMap(Function<? super T, ? extends Functor<? extends R>> fn) {
    return isEmpty() ? empty() : (Functor<R>) fn.apply(this.value);
  }

  public Functor<T> isTrue(Consumer<? super T> consumer) {
    if (isEmpty()) {
      return this;
    }
    Objects.requireNonNull(validator);
    if (validator.test(value)) {
      consumer.accept(value);
    }
    return this;
  }

  public Functor<T> isFalse(Consumer<? super T> consumer) {
    if (isEmpty()) {
      return this;
    }
    Objects.requireNonNull(validator);
    if (!validator.test(value)) {
      consumer.accept(value);
    }
    return this;
  }

  public Functor<T> validator(Predicate<? super T> validator) {
    this.validator = validator;
    return this;
  }

  public Functor<T> filter(Predicate<? super T> predicate) {
    return isEmpty() ? this : predicate.test(value) ? this : empty();
  }

  public void ifPresent(Consumer<? super T> consumer) {
    if (isPresent()) {
      consumer.accept(value);
    }
  }

  public void ifEmpty(Consumer<? super T> consumer) {
    if (isEmpty()) {
      consumer.accept(value);
    }
  }

  public T get() {
    if (value == null) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  @SuppressWarnings("unchecked")
  public Functor<T> or(Supplier<? extends Functor<? extends T>> supplier) {
    return isPresent() ? this : (Functor<T>) supplier.get();
  }

  public T orElse(T other) {
    return isPresent() ? value : other;
  }

  public T orElseGet(Supplier<? extends T> supplier) {
    return isPresent() ? value : supplier.get();
  }

  public T orElseThrow() {
    if (isEmpty()) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  public <X extends Throwable> T orElseThrow(Supplier<? extends X> supplier) throws X {
    if (isPresent()) {
      return value;
    } else {
      throw supplier.get();
    }
  }

  public Stream<T> stream() {
    if (!isPresent()) {
      return Stream.empty();
    } else {
      return Stream.of(value);
    }
  }

  public <R> Functor<R> infer(Supplier<R> trueHandler, Supplier<R> falseHandler) {
    Function<Predicate<? super T>, Functor<R>> supplier =
        (predicate) ->
            predicate.test(value)
                ? Functor.maybe(trueHandler).map(Supplier::get)
                : Functor.maybe(falseHandler).map(Supplier::get);
    return Functor.maybe(validator).map(supplier).orElse(empty());
  }
}

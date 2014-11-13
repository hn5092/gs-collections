/*
 * Copyright 2014 Goldman Sachs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gs.collections.impl.set.sorted.mutable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;

import com.gs.collections.api.LazyIterable;
import com.gs.collections.api.block.function.Function;
import com.gs.collections.api.block.function.Function2;
import com.gs.collections.api.block.function.primitive.BooleanFunction;
import com.gs.collections.api.block.function.primitive.ByteFunction;
import com.gs.collections.api.block.function.primitive.CharFunction;
import com.gs.collections.api.block.function.primitive.DoubleFunction;
import com.gs.collections.api.block.function.primitive.FloatFunction;
import com.gs.collections.api.block.function.primitive.IntFunction;
import com.gs.collections.api.block.function.primitive.LongFunction;
import com.gs.collections.api.block.function.primitive.ShortFunction;
import com.gs.collections.api.block.predicate.Predicate;
import com.gs.collections.api.block.predicate.Predicate2;
import com.gs.collections.api.block.procedure.Procedure;
import com.gs.collections.api.list.MutableList;
import com.gs.collections.api.list.primitive.MutableBooleanList;
import com.gs.collections.api.list.primitive.MutableByteList;
import com.gs.collections.api.list.primitive.MutableCharList;
import com.gs.collections.api.list.primitive.MutableDoubleList;
import com.gs.collections.api.list.primitive.MutableFloatList;
import com.gs.collections.api.list.primitive.MutableIntList;
import com.gs.collections.api.list.primitive.MutableLongList;
import com.gs.collections.api.list.primitive.MutableShortList;
import com.gs.collections.api.multimap.sortedset.MutableSortedSetMultimap;
import com.gs.collections.api.partition.set.sorted.PartitionMutableSortedSet;
import com.gs.collections.api.set.ParallelSetIterable;
import com.gs.collections.api.set.SetIterable;
import com.gs.collections.api.set.sorted.ImmutableSortedSet;
import com.gs.collections.api.set.sorted.MutableSortedSet;
import com.gs.collections.api.set.sorted.SortedSetIterable;
import com.gs.collections.api.stack.MutableStack;
import com.gs.collections.api.tuple.Pair;
import com.gs.collections.impl.collection.mutable.AbstractSynchronizedMutableCollection;
import com.gs.collections.impl.collection.mutable.SynchronizedCollectionSerializationProxy;
import com.gs.collections.impl.factory.SortedSets;
import com.gs.collections.impl.stack.mutable.ArrayStack;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * A synchronized view of a {@link MutableSortedSet}. It is imperative that the user manually synchronize on the collection when iterating over it using the
 * standard JDK iterator or JDK 5 for loop, as per {@link Collections#synchronizedCollection(Collection)}.
 *
 * @see MutableSortedSet#asSynchronized()
 */
@ThreadSafe
public class SynchronizedSortedSet<T>
        extends AbstractSynchronizedMutableCollection<T>
        implements MutableSortedSet<T>, Serializable
{
    SynchronizedSortedSet(MutableSortedSet<T> set)
    {
        super(set);
    }

    SynchronizedSortedSet(MutableSortedSet<T> set, Object newLock)
    {
        super(set, newLock);
    }

    /**
     * This method will take a MutableSortedSet and wrap it directly in a SynchronizedSortedSet.  It will
     * take any other non-GS-collection and first adapt it will a SortedSetAdapter, and then return a
     * SynchronizedSortedSet that wraps the adapter.
     */
    public static <E, S extends SortedSet<E>> SynchronizedSortedSet<E> of(S set)
    {
        return new SynchronizedSortedSet<E>(SortedSetAdapter.adapt(set));
    }

    /**
     * This method will take a MutableSortedSet and wrap it directly in a SynchronizedSortedSet.  It will
     * take any other non-GS-collection and first adapt it will a SortedSetAdapter, and then return a
     * SynchronizedSortedSet that wraps the adapter.  Additionally, a developer specifies which lock to use
     * with the collection.
     */
    public static <E, S extends SortedSet<E>> MutableSortedSet<E> of(S set, Object lock)
    {
        return new SynchronizedSortedSet<E>(SortedSetAdapter.adapt(set), lock);
    }

    @GuardedBy("getLock()")
    private MutableSortedSet<T> getSortedSet()
    {
        return (MutableSortedSet<T>) this.getCollection();
    }

    @Override
    public MutableSortedSet<T> asUnmodifiable()
    {
        synchronized (this.getLock())
        {
            return UnmodifiableSortedSet.of(this);
        }
    }

    @Override
    public ImmutableSortedSet<T> toImmutable()
    {
        synchronized (this.getLock())
        {
            return SortedSets.immutable.ofSortedSet(this.getSortedSet());
        }
    }

    public MutableStack<T> toStack()
    {
        synchronized (this.getLock())
        {
            return ArrayStack.newStack(this);
        }
    }

    @Override
    public MutableSortedSet<T> asSynchronized()
    {
        return this;
    }

    @Override
    public MutableSortedSet<T> clone()
    {
        synchronized (this.getLock())
        {
            return of(this.getSortedSet().clone());
        }
    }

    @Override
    public <V> MutableList<V> collect(Function<? super T, ? extends V> function)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().collect(function);
        }
    }

    @Override
    public MutableBooleanList collectBoolean(BooleanFunction<? super T> booleanFunction)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().collectBoolean(booleanFunction);
        }
    }

    @Override
    public MutableByteList collectByte(ByteFunction<? super T> byteFunction)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().collectByte(byteFunction);
        }
    }

    @Override
    public MutableCharList collectChar(CharFunction<? super T> charFunction)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().collectChar(charFunction);
        }
    }

    @Override
    public MutableDoubleList collectDouble(DoubleFunction<? super T> doubleFunction)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().collectDouble(doubleFunction);
        }
    }

    @Override
    public MutableFloatList collectFloat(FloatFunction<? super T> floatFunction)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().collectFloat(floatFunction);
        }
    }

    @Override
    public MutableIntList collectInt(IntFunction<? super T> intFunction)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().collectInt(intFunction);
        }
    }

    @Override
    public MutableLongList collectLong(LongFunction<? super T> longFunction)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().collectLong(longFunction);
        }
    }

    @Override
    public MutableShortList collectShort(ShortFunction<? super T> shortFunction)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().collectShort(shortFunction);
        }
    }

    @Override
    public <V> MutableList<V> flatCollect(Function<? super T, ? extends Iterable<V>> function)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().flatCollect(function);
        }
    }

    @Override
    public <V> MutableList<V> collectIf(
            Predicate<? super T> predicate,
            Function<? super T, ? extends V> function)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().collectIf(predicate, function);
        }
    }

    @Override
    public <P, V> MutableList<V> collectWith(Function2<? super T, ? super P, ? extends V> function, P parameter)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().collectWith(function, parameter);
        }
    }

    @Override
    public <V> MutableSortedSetMultimap<V, T> groupBy(Function<? super T, ? extends V> function)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().groupBy(function);
        }
    }

    @Override
    public <V> MutableSortedSetMultimap<V, T> groupByEach(Function<? super T, ? extends Iterable<V>> function)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().groupByEach(function);
        }
    }

    @Override
    public MutableSortedSet<T> newEmpty()
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().newEmpty().asSynchronized();
        }
    }

    @Override
    public MutableSortedSet<T> reject(Predicate<? super T> predicate)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().reject(predicate);
        }
    }

    @Override
    public <P> MutableSortedSet<T> rejectWith(Predicate2<? super T, ? super P> predicate, P parameter)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().rejectWith(predicate, parameter);
        }
    }

    @Override
    public MutableSortedSet<T> tap(Procedure<? super T> procedure)
    {
        synchronized (this.getLock())
        {
            this.forEach(procedure);
            return this;
        }
    }

    @Override
    public MutableSortedSet<T> select(Predicate<? super T> predicate)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().select(predicate);
        }
    }

    @Override
    public <P> MutableSortedSet<T> selectWith(Predicate2<? super T, ? super P> predicate, P parameter)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().selectWith(predicate, parameter);
        }
    }

    @Override
    public PartitionMutableSortedSet<T> partition(Predicate<? super T> predicate)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().partition(predicate);
        }
    }

    @Override
    public <P> PartitionMutableSortedSet<T> partitionWith(Predicate2<? super T, ? super P> predicate, P parameter)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().partitionWith(predicate, parameter);
        }
    }

    public PartitionMutableSortedSet<T> partitionWhile(Predicate<? super T> predicate)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().partitionWhile(predicate);
        }
    }

    @Override
    public <S> MutableSortedSet<S> selectInstancesOf(Class<S> clazz)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().selectInstancesOf(clazz);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().equals(obj);
        }
    }

    @Override
    public int hashCode()
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().hashCode();
        }
    }

    @Override
    public <S> MutableList<Pair<T, S>> zip(Iterable<S> that)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().zip(that);
        }
    }

    @Override
    public <S, R extends Collection<Pair<T, S>>> R zip(Iterable<S> that, R target)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().zip(that, target);
        }
    }

    @Override
    public MutableSortedSet<Pair<T, Integer>> zipWithIndex()
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().zipWithIndex();
        }
    }

    @Override
    public <R extends Collection<Pair<T, Integer>>> R zipWithIndex(R target)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().zipWithIndex(target);
        }
    }

    public MutableSortedSet<T> takeWhile(Predicate<? super T> predicate)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().takeWhile(predicate);
        }
    }

    public MutableSortedSet<T> dropWhile(Predicate<? super T> predicate)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().dropWhile(predicate);
        }
    }

    public MutableSortedSet<T> distinct()
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().distinct();
        }
    }

    public Comparator<? super T> comparator()
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().comparator();
        }
    }

    public MutableSortedSet<T> union(SetIterable<? extends T> set)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().union(set);
        }
    }

    public <R extends Set<T>> R unionInto(SetIterable<? extends T> set, R targetSet)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().unionInto(set, targetSet);
        }
    }

    public MutableSortedSet<T> intersect(SetIterable<? extends T> set)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().intersect(set);
        }
    }

    public <R extends Set<T>> R intersectInto(SetIterable<? extends T> set, R targetSet)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().intersectInto(set, targetSet);
        }
    }

    public MutableSortedSet<T> difference(SetIterable<? extends T> subtrahendSet)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().difference(subtrahendSet);
        }
    }

    public <R extends Set<T>> R differenceInto(SetIterable<? extends T> subtrahendSet, R targetSet)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().differenceInto(subtrahendSet, targetSet);
        }
    }

    public MutableSortedSet<T> symmetricDifference(SetIterable<? extends T> setB)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().symmetricDifference(setB);
        }
    }

    public <R extends Set<T>> R symmetricDifferenceInto(SetIterable<? extends T> set, R targetSet)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().symmetricDifferenceInto(set, targetSet);
        }
    }

    public boolean isSubsetOf(SetIterable<? extends T> candidateSuperset)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().isSubsetOf(candidateSuperset);
        }
    }

    public boolean isProperSubsetOf(SetIterable<? extends T> candidateSuperset)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().isProperSubsetOf(candidateSuperset);
        }
    }

    public MutableSortedSet<SortedSetIterable<T>> powerSet()
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().powerSet();
        }
    }

    public <B> LazyIterable<Pair<T, B>> cartesianProduct(SetIterable<B> set)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().cartesianProduct(set);
        }
    }

    public MutableSortedSet<T> subSet(T fromElement, T toElement)
    {
        synchronized (this.getLock())
        {
            return of(this.getSortedSet().subSet(fromElement, toElement), this.getLock());
        }
    }

    public MutableSortedSet<T> headSet(T toElement)
    {
        synchronized (this.getLock())
        {
            return of(this.getSortedSet().headSet(toElement), this.getLock());
        }
    }

    public MutableSortedSet<T> tailSet(T fromElement)
    {
        synchronized (this.getLock())
        {
            return of(this.getSortedSet().tailSet(fromElement), this.getLock());
        }
    }

    public T first()
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().first();
        }
    }

    public T last()
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().last();
        }
    }

    public int compareTo(SortedSetIterable<T> o)
    {
        synchronized (this.getLock())
        {
            return this.getSortedSet().compareTo(o);
        }
    }

    @Override
    public MutableSortedSet<T> with(T element)
    {
        this.add(element);
        return this;
    }

    @Override
    public MutableSortedSet<T> without(T element)
    {
        this.remove(element);
        return this;
    }

    @Override
    public MutableSortedSet<T> withAll(Iterable<? extends T> elements)
    {
        this.addAllIterable(elements);
        return this;
    }

    @Override
    public MutableSortedSet<T> withoutAll(Iterable<? extends T> elements)
    {
        this.removeAllIterable(elements);
        return this;
    }

    protected Object writeReplace()
    {
        return new SynchronizedCollectionSerializationProxy<T>(this.getSortedSet());
    }

    public ParallelSetIterable<T> asParallel(ExecutorService executorService, int batchSize)
    {
        throw new UnsupportedOperationException("asParallel() method is not supported for " + this.getClass().getSimpleName() + '.');
    }
}

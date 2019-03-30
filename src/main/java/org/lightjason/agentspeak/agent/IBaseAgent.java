/*
 * @cond LICENSE
 * ######################################################################################
 * # LGPL License                                                                       #
 * #                                                                                    #
 * # This file is part of the LightJason                                                #
 * # Copyright (c) 2015-19, LightJason (info@lightjason.org)                            #
 * # This program is free software: you can redistribute it and/or modify               #
 * # it under the terms of the GNU Lesser General Public License as                     #
 * # published by the Free Software Foundation, either version 3 of the                 #
 * # License, or (at your option) any later version.                                    #
 * #                                                                                    #
 * # This program is distributed in the hope that it will be useful,                    #
 * # but WITHOUT ANY WARRANTY; without even the implied warranty of                     #
 * # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                      #
 * # GNU Lesser General Public License for more details.                                #
 * #                                                                                    #
 * # You should have received a copy of the GNU Lesser General Public License           #
 * # along with this program. If not, see http://www.gnu.org/licenses/                  #
 * ######################################################################################
 * @endcond
 */

package org.lightjason.agentspeak.agent;

import com.codepoetics.protonpack.StreamUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lightjason.agentspeak.beliefbase.view.IView;
import org.lightjason.agentspeak.common.IPath;
import org.lightjason.agentspeak.configuration.IAgentConfiguration;
import org.lightjason.agentspeak.error.CNoSuchElementException;
import org.lightjason.agentspeak.language.CCommon;
import org.lightjason.agentspeak.language.CLiteral;
import org.lightjason.agentspeak.language.ILiteral;
import org.lightjason.agentspeak.language.IStructureHash;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.execution.IContext;
import org.lightjason.agentspeak.language.execution.IVariableBuilder;
import org.lightjason.agentspeak.language.execution.instantiable.plan.statistic.CPlanStatistic;
import org.lightjason.agentspeak.language.execution.instantiable.plan.statistic.IPlanStatistic;
import org.lightjason.agentspeak.language.execution.instantiable.plan.trigger.ITrigger;
import org.lightjason.agentspeak.language.execution.instantiable.rule.IRule;
import org.lightjason.agentspeak.language.fuzzy.IFuzzyValue;
import org.lightjason.agentspeak.language.fuzzy.bundle.IFuzzyBundle;
import org.lightjason.agentspeak.language.unifier.IUnifier;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * agent base structure
 *
 * @tparam T agent type
 */
public abstract class IBaseAgent<T extends IAgent<?>> implements IAgent<T>
{
    /**
     * logger
     */
    protected static final Logger LOGGER = org.lightjason.agentspeak.common.CCommon.logger( IAgent.class );
    /**
     * serial id
     */
    private static final long serialVersionUID = -304366902555398136L;
    /**
     * beliefbase
     */
    protected final IView m_beliefbase;
    /**
     * storage map
     *
     * @note must be thread-safe
     */
    protected final Map<String, Object> m_storage = new ConcurrentHashMap<>();
    /**
     * execution trigger with content hash
     */
    protected final Map<Integer, ITrigger> m_trigger = new ConcurrentHashMap<>();
    /**
     * multimap with rules
     */
    protected final Multimap<IPath, IRule> m_rules = Multimaps.synchronizedMultimap( LinkedHashMultimap.create() );
    /**
     * map with all existing plans and successful / fail runs
     */
    @SuppressWarnings( "RedundantTypeArguments" )
    protected final Multimap<ITrigger, IPlanStatistic> m_plans = Multimaps.synchronizedMultimap(
        TreeMultimap.create( IStructureHash.COMPARATOR, Comparator.<IPlanStatistic>naturalOrder() ) );
    /**
     * nano seconds at the last cycle
     */
    private final AtomicLong m_cycletime = new AtomicLong();
    /**
     * number of sleeping cycles
     *
     * @note values >= 0 defines the sleeping time, Long.MAX_VALUE is infinity sleeping
     * negative values defines the activity
     */
    private final AtomicLong m_sleepingcycles = new AtomicLong( Long.MIN_VALUE );
    /**
     * set for waking-up literals
     */
    private final Set<ITerm> m_sleepingterm = Collections.synchronizedSet( new HashSet<>() );
    /**
     * unifier
     */
    private final IUnifier m_unifier;
    /**
     * variable builder
     */
    private final IVariableBuilder m_variablebuilder;
    /**
     * fuzzy result collector
     */
    private final IFuzzyBundle m_fuzzy;
    /**
     * running plans (thread-safe)
     */
    private final Multimap<IPath, ILiteral> m_runningplans = Multimaps.synchronizedSetMultimap( HashMultimap.create() );



    /**
     * ctor
     *
     * @param p_configuration agent configuration
     */
    public IBaseAgent( @Nonnull final IAgentConfiguration<T> p_configuration )
    {
        // initialize agent
        m_unifier = p_configuration.unifier();
        m_beliefbase = p_configuration.beliefbase();
        m_variablebuilder = p_configuration.variablebuilder();
        m_fuzzy = p_configuration.fuzzy();

        // initial plans and rules
        p_configuration.plans().parallelStream().forEach( i -> m_plans.put( i.trigger(), CPlanStatistic.of( i ) ) );
        p_configuration.rules().parallelStream().forEach( i -> m_rules.put( i.identifier().fqnfunctor(), i ) );
        if ( !ITrigger.EMPTY.equals( p_configuration.initialgoal() ) )
            m_trigger.put( p_configuration.initialgoal().hashCode(), p_configuration.initialgoal() );
    }

    @Nonnull
    @Override
    public final IView beliefbase()
    {
        return m_beliefbase;
    }

    @Nonnull
    @Override
    public final IAgent<T> inspect( @Nonnull final IInspector... p_inspector )
    {
        Arrays.stream( p_inspector )
              .parallel()
              .peek( i -> i.inspectcycletime( m_cycletime.get() ) )
              .peek( i -> i.inspectsleeping( m_sleepingcycles.get() ) )
              .peek( i -> i.inspectbelief( m_beliefbase.stream() ) )
              .peek( i -> i.inspectplans( m_plans.values().stream() ) )
              .peek( i -> i.inspectrunningplans( m_runningplans.values().stream() ) )
              .peek( i -> i.inspectstorage( m_storage.entrySet().stream() ) )
              .peek( i -> i.inspectrules( m_rules.values().stream() ) )
              .forEach( i -> i.inspectpendingtrigger( m_trigger.values().stream() ) );

        return this;
    }

    @Nonnull
    @Override
    public final Multimap<IPath, ILiteral> runningplans()
    {
        return ImmutableMultimap.copyOf( m_runningplans );
    }

    @Override
    public final boolean sleeping()
    {
        return m_sleepingcycles.get() > 0;
    }

    @Nonnull
    @Override
    public final IAgent<T> sleep( final long p_cycles, final ITerm... p_term )
    {
        return this.sleep(
            p_cycles,
            ( Objects.isNull( p_term ) ) || ( p_term.length == 0 )
            ? Stream.of()
            : Arrays.stream( p_term )
        );
    }

    @Nonnull
    @Override
    public final IAgent<T> sleep( final long p_cycles, @Nonnull final Stream<ITerm> p_literal )
    {
        m_sleepingcycles.set( p_cycles );
        p_literal.filter( i -> !i.hasVariable() ).forEach( m_sleepingterm::add );
        return this;
    }

    @Nonnull
    @Override
    public final IAgent<T> wakeup( @Nullable final ITerm... p_term )
    {
        return this.wakeup(
            ( Objects.isNull( p_term ) ) || ( p_term.length == 0 )
            ? Stream.of()
            : Arrays.stream( p_term )
        );
    }

    @Nonnull
    @Override
    public final IAgent<T> wakeup( @Nonnull final Stream<ITerm> p_term )
    {
        p_term.forEach( m_sleepingterm::add );
        this.active( true );
        return this;
    }

    @Nonnull
    @Override
    public final Map<String, Object> storage()
    {
        return m_storage;
    }

    @Nonnull
    @Override
    public final IUnifier unifier()
    {
        return m_unifier;
    }

    @Nonnegative
    @Override
    public final long cycletime()
    {
        return m_cycletime.get();
    }

    @Nonnull
    @Override
    public final Multimap<ITrigger, IPlanStatistic> plans()
    {
        return m_plans;
    }

    @Nonnull
    @Override
    public final IFuzzyBundle fuzzy()
    {
        return m_fuzzy;
    }

    @Nonnull
    @Override
    public final IVariableBuilder variablebuilder()
    {
        return m_variablebuilder;
    }

    @Nonnull
    @Override
    public final Multimap<IPath, IRule> rules()
    {
        return m_rules;
    }

    @Nonnull
    @Override
    @SuppressWarnings( "unchecked" )
    public final <N extends IAgent<?>> N raw()
    {
        return (N) this;
    }

    @Override
    public String toString()
    {
        return MessageFormat.format(
            "{0} ( {1} )",
            super.toString(),
            StringUtils.join(
                StreamUtils.zip(
                    Stream.of( "Trigger", "Running Plans", "Beliefbase" ),
                    Stream.of( m_trigger.values(), m_runningplans.keySet(), m_beliefbase ),
                    ( l, c ) -> MessageFormat.format( "{0}: {1}", l, c )
                ).toArray(),
                " / "
            )
        );
    }

    @Nonnull
    @Override
    public final Stream<IFuzzyValue<?>> trigger( @Nonnull final ITrigger p_trigger, @Nullable final boolean... p_immediately )
    {
        if ( m_sleepingcycles.get() > 0 )
            return m_fuzzy.membership().fail();

        // check if literal does not store any variables
        if ( p_trigger.literal().hasVariable() )
            throw new CNoSuchElementException( org.lightjason.agentspeak.common.CCommon.languagestring( IBaseAgent.class, "literalvariable", p_trigger ) );

        // run plan immediatly and return
        if ( Objects.nonNull( p_immediately ) && p_immediately.length > 0 && p_immediately[0] )
            return this.execute( this.generateexecution( Stream.of( p_trigger ) ) );

        // add trigger for the next cycle must be synchronized to avoid indeterministic state during execution
        synchronized ( this )
        {
            m_trigger.putIfAbsent( p_trigger.hashCode(), p_trigger );
        }

        return m_fuzzy.membership().success();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public T call() throws Exception
    {
        // run beliefbase update, because environment can be changed and decrement sleeping value
        m_beliefbase.update( this );
        if ( !this.active( false ) )
            // check wakup-event otherwise suspend
            return (T) this;

        // update fuzzification
        m_fuzzy.update( this );

        // clear running plan- and trigger list and execute elements
        this.execute( this.generateexecutionlist() )
            .forEach( i ->
            {
            } );


        // set the cycle time
        m_cycletime.set( System.nanoTime() );

        return (T) this;
    }

    /**
     * create the plan executionlist with clearing internal structures
     *
     * @return collection with execution plan and context
     *
     * @note must be synchronized for avoid indeterministic trigger list
     */
    @Nonnull
    private synchronized Collection<Pair<IPlanStatistic, IContext>> generateexecutionlist()
    {
        m_runningplans.clear();
        final Collection<Pair<IPlanStatistic, IContext>> l_execution = this.generateexecution(
            Stream.concat(
                m_trigger.values().parallelStream(),
                m_beliefbase.trigger().parallel()
            )
        );
        m_trigger.clear();

        return l_execution;
    }


    /**
     * create execution list with plan and context
     *
     * @param p_trigger trigger stream
     * @return collection with excutable plans, instantiated execution context and plan statistic
     */
    @Nonnull
    private Collection<Pair<IPlanStatistic, IContext>> generateexecution( @Nonnull final Stream<ITrigger> p_trigger )
    {
        return p_trigger
            // get all possible plans
            .flatMap( i -> m_plans.get( i ).stream().map( j -> new ImmutablePair<>( i, j ) ) )
            .parallel()
            // tries to unifier trigger literal and filter of valid unification (returns set of unified variables)
            .map( i -> new ImmutablePair<>( i, CCommon.unifytrigger( m_unifier, i.getLeft(), i.getRight().plan().trigger() ) ) )
            // check if unification was possible
            .filter( i -> i.getRight().getLeft() )
            // create execution context
            .map( i -> CCommon.instantiateplan( i.getLeft().getRight(), this, i.getRight().getRight() ) )
            // check plan-condition
            .filter( i -> i.getLeft().plan().condition( i.getRight() ) )
            // collectors-call must be toList not toSet because plan-execution can be have equal elements
            // so a set avoid multiple plan-execution
            .collect( Collectors.toList() );
    }

    /**
     * execute list of plans
     *
     * @param p_execution execution collection with instantiated plans and context
     * @return fuzzy result for each executaed plan
     */
    @Nonnull
    private Stream<IFuzzyValue<?>> execute( @Nonnull final Collection<Pair<IPlanStatistic, IContext>> p_execution )
    {
        // update executable plan list, so that test-goals are defined all the time
        p_execution.parallelStream().forEach( i -> m_runningplans.put(
            i.getLeft().plan().trigger().literal().fqnfunctor(),
            i.getLeft().plan().trigger().literal().allocate( i.getRight() )
        ) );

        // execute plan and return values and return execution result
        return p_execution.parallelStream()
                          .flatMap( i ->
                          {
                              // execute plan
                              final Number l_result = i.getRight().agent().fuzzy().defuzzification().apply(
                                  i.getLeft().plan().execute( false, i.getRight(), Collections.emptyList(), Collections.emptyList() )
                              );

                              // check strict execution result
                              if ( i.getRight().agent().fuzzy().defuzzification().success( l_result ) )
                              {
                                  i.getLeft().incrementsuccessful();
                                  return i.getRight().agent().fuzzy().membership().success();
                              }
                              else
                              {
                                  i.getLeft().incrementfail();
                                  i.getRight().agent().trigger(
                                      ITrigger.EType.DELETEGOAL.builddefault( i.getLeft().plan().literal().allocate( i.getRight() ) )
                                  );
                                  return i.getRight().agent().fuzzy().membership().fail();
                              }

                          } );
    }

    /**
     * runs the wakeup goal
     *
     * @param p_immediatly runs the wake always
     * @return returns true if the agent is active
     */
    private boolean active( final boolean p_immediatly )
    {
        // if the sleeping time ends or the agent will wakedup by a hard call,
        // create the trigger and reset the time value
        if ( m_sleepingcycles.compareAndSet( 0, Long.MIN_VALUE ) || p_immediatly )
        {
            (
                m_sleepingterm.isEmpty()

                ? Stream.of( ITrigger.EType.ADDGOAL.builddefault( CLiteral.of( "wakeup" ) ) )

                : m_sleepingterm.parallelStream()
                                .map( i -> ITrigger.EType.ADDGOAL.builddefault( CLiteral.of( "wakeup", i ) ) )

            ).forEach( i -> m_trigger.put( i.structurehash(), i ) );

            m_sleepingterm.clear();
        }

        // if the sleeping time is not infinity decrese the counter
        if ( m_sleepingcycles.get() > 0 && m_sleepingcycles.get() != Long.MAX_VALUE )
            m_sleepingcycles.decrementAndGet();

        return m_sleepingcycles.get() <= 0;
    }

}

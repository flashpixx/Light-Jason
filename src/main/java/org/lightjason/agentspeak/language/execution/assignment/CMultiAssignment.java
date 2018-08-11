/*
 * @cond LICENSE
 * ######################################################################################
 * # LGPL License                                                                       #
 * #                                                                                    #
 * # This file is part of the LightJason AgentSpeak(L++)                                #
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

package org.lightjason.agentspeak.language.execution.assignment;

import org.lightjason.agentspeak.language.CCommon;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.execution.IBaseExecution;
import org.lightjason.agentspeak.language.execution.IContext;
import org.lightjason.agentspeak.language.execution.IExecution;
import org.lightjason.agentspeak.language.fuzzy.CFuzzyValue;
import org.lightjason.agentspeak.language.fuzzy.IFuzzyValue;
import org.lightjason.agentspeak.language.variable.IVariable;

import javax.annotation.Nonnull;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * assignment action of a multi-variable list
 */
public final class CMultiAssignment extends IBaseExecution<List<IVariable<?>>>
{
    /**
     * serial id
     */
    private static final long serialVersionUID = -6123210880356077509L;
    /**
     * right-hand argument
     */
    private final IExecution m_righthand;

    /**
     * ctor
     *
     * @param p_lefthand left-hand variable list
     * @param p_righthand right-hand argument
     */
    public CMultiAssignment( @Nonnull final Stream<IVariable<?>> p_lefthand, @Nonnull final IExecution p_righthand )
    {
        super( Collections.unmodifiableList( p_lefthand.collect( Collectors.toList() ) ) );
        m_righthand = p_righthand;
    }

    @Nonnull
    @Override
    public IFuzzyValue<Boolean> execute( final boolean p_parallel, @Nonnull final IContext p_context,
                                         @Nonnull final List<ITerm> p_argument, @Nonnull final List<ITerm> p_return )
    {
        final List<ITerm> l_result = CCommon.argumentlist();

        if ( !m_righthand.execute( p_parallel, p_context, Collections.<ITerm>emptyList(), l_result ).value() || l_result.isEmpty() )
            return CFuzzyValue.of( false );


        // position matching on list index
        final List<ITerm> l_flatresult = CCommon.flatten( l_result ).collect( Collectors.toList() );
        final List<ITerm> l_assign = CCommon.replacebycontext( p_context, m_value.stream() ).collect( Collectors.toList() );

        IntStream.range( 0, Math.min( l_assign.size(), l_flatresult.size() ) )
                 .boxed()
                 .forEach( i -> l_assign.get( i ).<IVariable<Object>>term().set( l_flatresult.get( i ).raw() ) );


        // tail matching
        if ( l_assign.size() < l_flatresult.size() )
            l_assign.get( l_assign.size() - 1 ).<IVariable<Object>>term().set( l_flatresult.subList( l_assign.size() - 1, l_flatresult.size() ) );

        return CFuzzyValue.of( true );
    }

    @Override
    public int hashCode()
    {
        return super.hashCode() ^ m_righthand.hashCode();
    }

    @Override
    public boolean equals( final Object p_object )
    {
        return p_object instanceof IExecution && this.hashCode() == p_object.hashCode();
    }

    @Override
    public String toString()
    {
        return MessageFormat.format( "{0} = {1}", m_value, m_righthand );
    }

    @Nonnull
    @Override
    public Stream<IVariable<?>> variables()
    {
        return Stream.concat(
            Objects.isNull( m_value ) ? Stream.empty() : m_value.stream(),
            m_righthand.variables()
        );
    }
}
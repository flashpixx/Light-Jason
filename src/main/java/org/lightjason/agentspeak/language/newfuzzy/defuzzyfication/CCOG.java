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

package org.lightjason.agentspeak.language.newfuzzy.defuzzyfication;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.lightjason.agentspeak.language.newfuzzy.IFuzzyValue;
import org.lightjason.agentspeak.language.newfuzzy.membership.IFuzzyMembership;
import org.lightjason.agentspeak.language.newfuzzy.set.IFuzzySet;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.Stream;


/**
 * defuzzification with center-of-gravity
 *
 * @tparam E fuzzy set enum type
 */
public final class CCOG<E extends Enum<?>> extends IBaseDefuzzification<E>
{
    /**
     * fuzzy membership function
     */
    private final IFuzzyMembership<E> m_membership;

    /**
     * ctor
     *
     * @param p_class fuzzy set class
     * @param p_default fuzzy enum type
     */
    public CCOG( @NonNull final Class<? extends IFuzzySet<E>> p_class, @NonNull final E p_default, @NonNull final IFuzzyMembership<E> p_membership )
    {
        super( p_class, p_default );
        m_membership = p_membership;
    }

    /**
     * ctor
     *
     * @param p_class fuzzy set class
     * @param p_default fuzzy enum type
     * @param p_success success function
     */
    public CCOG( @NonNull final Class<? extends IFuzzySet<E>> p_class, @NonNull final E p_default, @NonNull final BiFunction<E, Class<? extends IFuzzySet<E>>, Boolean> p_success,
                 @NonNull final IFuzzyMembership<E> p_membership )
    {
        super( p_class, p_default, p_success );
        m_membership = p_membership;
    }

    @Nonnull
    @Override
    public E defuzzify( @Nonnull final Stream<IFuzzyValue<?>> p_value )
    {
        final IFuzzyValue<?>[] l_values = p_value.toArray( IFuzzyValue<?>[]::new );
        if ( l_values.length < 2 )
            return l_values.length == 0 ? m_default : m_class.getEnumConstants()[l_values[0].get().ordinal()].get();


        // gets the maximum value of all membership functions
        final Number l_max = Arrays.stream( m_class.getEnumConstants() )
                                   .mapToDouble( i -> m_membership.range( this.index2enum( i.get().ordinal() ) )
                                                                  .mapToDouble( Number::doubleValue )
                                                                  .max()
                                                                  .orElse( 0 ) )
                                   .max()
                                   .orElse( 1 );

        // calculate the gravity of the given values
        final Number l_result = Arrays.stream( l_values )
                                      .mapToDouble( i -> m_membership.range( this.index2enum( i.get().ordinal() ) ).mapToDouble( Number::doubleValue ).sum()
                                                         * i.fuzzy().doubleValue() )
                                      .sum()
                                / Arrays.stream( l_values ).mapToDouble( i -> m_membership.range( this.index2enum( i.get().ordinal() ) ).count()
                                                                              * i.fuzzy().doubleValue() ).sum();

        // scale the gravity on the maximum to the enum result
        return this.index2enum( (int)( l_result.doubleValue() / l_max.doubleValue() * ( m_class.getEnumConstants().length - 1 ) ) );
    }

}

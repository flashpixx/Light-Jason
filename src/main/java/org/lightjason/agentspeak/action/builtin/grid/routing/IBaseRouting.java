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

package org.lightjason.agentspeak.action.builtin.grid.routing;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tobject.ObjectMatrix2D;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.BiFunction;


/**
 * base routing structure
 */
public abstract class IBaseRouting implements IRouting
{
    /**
     * walkable function
     */
    protected final BiFunction<ObjectMatrix2D, DoubleMatrix1D, Boolean> m_walkable;

    /**
     * ctor
     */
    protected IBaseRouting()
    {
        this( ( g, p ) -> Objects.nonNull( g.getQuick( (int) p.getQuick( 0 ), (int) p.getQuick( 1 ) ) ) );
    }

    /**
     * ctor
     *
     * @param p_walkable walkable function
     */
    protected IBaseRouting( @NonNull final BiFunction<ObjectMatrix2D, DoubleMatrix1D, Boolean> p_walkable )
    {
        m_walkable = p_walkable;
    }

    /**
     * check walkable position e.g. by grid size
     *
     * @param p_grid grid
     * @param p_current current position
     * @param p_direction walkable direction
     * @return pair of walkable and position vector
     */
    protected final Pair<Boolean, DoubleMatrix1D> walkable( @Nonnull final ObjectMatrix2D p_grid, @Nonnull final DoubleMatrix1D p_current,
                                                            @Nonnull final EDirection p_direction )
    {
        final DoubleMatrix1D l_position = p_direction.apply( p_current );
        return new ImmutablePair<>(
            l_position.getQuick( 0 ) >= 0 && l_position.getQuick( 0 ) < p_grid.rows()
            && l_position.getQuick( 1 ) >= 0 && l_position.getQuick( 1 ) < p_grid.columns()
            && m_walkable.apply( p_grid, l_position ),
            l_position
        );
    }

}
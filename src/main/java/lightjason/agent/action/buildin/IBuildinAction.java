/**
 * @cond LICENSE
 * ######################################################################################
 * # LGPL License                                                                       #
 * #                                                                                    #
 * # This file is part of the Light-Jason                                               #
 * # Copyright (c) 2015-16, Philipp Kraus (philipp.kraus@tu-clausthal.de)               #
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

package lightjason.agent.action.buildin;

import lightjason.agent.action.IBaseAction;
import lightjason.common.CPath;

import java.util.Arrays;
import java.util.List;


/**
 * base class of build-in actions for setting name
 * by package/classname (without prefix character)
 */
public abstract class IBuildinAction extends IBaseAction
{
    /**
     * action name
     */
    private final CPath m_name;

    /**
     * ctor
     */
    protected IBuildinAction()
    {
        this( 2 );
    }

    /**
     * ctor
     *
     * @param p_length length of package parts
     */
    protected IBuildinAction( final int p_length )
    {
        final List<String> l_names = Arrays.asList( this.getClass().getCanonicalName().split( "\\." ) );
        l_names.set( l_names.size() - 1, l_names.get( l_names.size() - 1 ).substring( 1 ) );

        m_name = new CPath( l_names.subList( Math.max( 0, l_names.size() - p_length ), l_names.size() ) ).toLower();
    }


    @Override
    public final CPath getName()
    {
        return m_name;
    }
}

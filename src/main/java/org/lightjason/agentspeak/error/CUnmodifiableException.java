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

package org.lightjason.agentspeak.error;

import org.lightjason.agentspeak.common.CCommon;

import javax.annotation.Nonnull;
import java.util.logging.Logger;


/**
 * unmodifiable exception
 */
public final class CUnmodifiableException extends IllegalStateException implements IException
{
    /**
     * logger
     */
    private static final Logger LOGGER = CCommon.logger( CUnmodifiableException.class );
    /**
     * serial uid
     */
    private static final transient long serialVersionUID = 5813639345533228283L;

    /**
     * ctor
     */
    public CUnmodifiableException()
    {
        super();
        LOGGER.warning( "exception is thrown" );
    }

    /**
     * ctor
     *
     * @param p_message message
     */
    public CUnmodifiableException( @Nonnull final String p_message )
    {
        super( p_message );
        LOGGER.warning( p_message );
    }

    /**
     * ctor
     *
     * @param p_message message
     * @param p_cause throwable
     */
    public CUnmodifiableException( @Nonnull final String p_message, @Nonnull final Throwable p_cause )
    {
        super( p_message, p_cause );
        LOGGER.warning( p_message );
    }

    /**
     * ctor
     *
     * @param p_cause throwable
     */
    public CUnmodifiableException( @Nonnull final Throwable p_cause )
    {
        super( p_cause );
        LOGGER.warning( p_cause.getMessage() );
    }
}

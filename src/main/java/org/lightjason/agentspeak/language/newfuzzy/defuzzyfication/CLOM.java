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

import org.lightjason.agentspeak.agent.IAgent;
import org.lightjason.agentspeak.language.fuzzy.IFuzzyValue;
import org.lightjason.agentspeak.language.newfuzzy.set.IFuzzySet;

import javax.annotation.Nonnull;
import java.util.stream.Stream;


/**
 * defuzzification with least-of-maximum
 */
public final class CLOM implements IDefuzzification
{

    @Nonnull
    @Override
    public IFuzzySet<?> defuzzify( @Nonnull final Stream<IFuzzyValue<?>> p_value
    )
    {
        return null;
    }

    @Override
    public boolean execution( @Nonnull final Stream<IFuzzySet<?>> p_value )
    {
        return false;
    }

    @Nonnull
    @Override
    public IAgent<?> update( @Nonnull final IAgent<?> p_agent )
    {
        return null;
    }
}

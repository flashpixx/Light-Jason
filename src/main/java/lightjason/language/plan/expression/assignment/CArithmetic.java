/**
 * @cond LICENSE
 * ######################################################################################
 * # GPL License                                                                        #
 * #                                                                                    #
 * # This file is part of the Light-Jason                                               #
 * # Copyright (c) 2015, Philipp Kraus (philipp.kraus@tu-clausthal.de)                  #
 * # This program is free software: you can redistribute it and/or modify               #
 * # it under the terms of the GNU General Public License as                            #
 * # published by the Free Software Foundation, either version 3 of the                 #
 * # License, or (at your option) any later version.                                    #
 * #                                                                                    #
 * # This program is distributed in the hope that it will be useful,                    #
 * # but WITHOUT ANY WARRANTY; without even the implied warranty of                     #
 * # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                      #
 * # GNU General Public License for more details.                                       #
 * #                                                                                    #
 * # You should have received a copy of the GNU General Public License                  #
 * # along with this program. If not, see http://www.gnu.org/licenses/                  #
 * ######################################################################################
 * @endcond
 */

package lightjason.language.plan.expression.assignment;

import lightjason.beliefbase.IBeliefBaseMask;
import lightjason.language.CVariable;
import lightjason.language.IVariable;
import lightjason.language.plan.IOperation;
import lightjason.language.plan.expression.IExpression;
import lightjason.language.plan.expression.arithmetic.CExpression;


/**
 * assignment of arithmetic expression
 */
public class CArithmetic implements IAssignment<CExpression>, IOperation
{
    /**
     * variable
     */
    private final CVariable<?> m_variable;
    /**
     * expression
     */
    private final IExpression m_expression;

    /**
     * ctor
     *
     * @param p_variable assign variable
     * @param p_expression evalute expression
     */
    public CArithmetic( final CVariable<?> p_variable, final CExpression p_expression )
    {
        m_variable = p_variable;
        m_expression = p_expression;
    }


    @Override
    public boolean evaluate( final IBeliefBaseMask p_beliefbase )
    {
        return true;
    }

    @Override
    public boolean assign( final IVariable<?> p_variable, final CExpression p_term )
    {
        return true;
    }
}

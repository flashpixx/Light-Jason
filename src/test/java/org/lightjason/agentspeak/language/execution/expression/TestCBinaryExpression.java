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

package org.lightjason.agentspeak.language.execution.expression;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lightjason.agentspeak.IBaseTest;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.execution.IContext;
import org.lightjason.agentspeak.language.execution.passing.CPassRaw;
import org.lightjason.agentspeak.language.execution.passing.CPassVariable;
import org.lightjason.agentspeak.language.variable.CVariable;
import org.lightjason.agentspeak.language.variable.IVariable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;


/**
 * test binary expression
 */
@RunWith( DataProviderRunner.class )
public final class TestCBinaryExpression extends IBaseTest
{
    /**
     * assignment operator
     *
     * @return 4-tuple as array, first & second input values, 3rd operator, 4th result
     */
    @DataProvider
    public static Object[] operator()
    {
        final Object l_object = new Object();

        return Stream.of(

            testcase( 5, 2, EBinaryOperator.PLUS, 7.0 ),
            testcase( 3, 5, EBinaryOperator.MINUS, -2.0 ),

            testcase( 12, 10, EBinaryOperator.MULTIPLY, 120.0 ),
            testcase( 360, 12, EBinaryOperator.DIVIDE, 30.0 ),
            testcase( 2, 8, EBinaryOperator.POWER, 256.0 ),

            testcase( 21, 17, EBinaryOperator.MODULO, 4L ),
            testcase( -1, 17, EBinaryOperator.MODULO, 16L ),
            testcase( -18, 17, EBinaryOperator.MODULO,  1L ),

            testcase( 1, 2, EBinaryOperator.LESS,  true ),
            testcase( 2, 3, EBinaryOperator.LESSEQUAL,  true ),
            testcase( 3, 3, EBinaryOperator.LESSEQUAL,  true ),
            testcase( 3, 2, EBinaryOperator.LESSEQUAL,  false ),

            testcase( 8, 6, EBinaryOperator.GREATER,  true ),
            testcase( 8, 6, EBinaryOperator.GREATEREQUAL,  true ),
            testcase( 8, 8, EBinaryOperator.GREATEREQUAL,  true ),
            testcase( 6, 8, EBinaryOperator.GREATEREQUAL,  false ),

            testcase( new Object(), new Object(), EBinaryOperator.EQUAL,  false ),
            testcase( l_object, l_object, EBinaryOperator.EQUAL,  true ),
            testcase( new Object(), new Object(), EBinaryOperator.NOTEQUAL,  true ),
            testcase( l_object, l_object, EBinaryOperator.NOTEQUAL,  false ),

            testcase( 5, 5, EBinaryOperator.EQUAL,  true ),
            testcase( 5.0, 5, EBinaryOperator.EQUAL,  true ),
            testcase( 5.0000, 5.0000001, EBinaryOperator.NOTEQUAL,  true ),

            testcase( true, true, EBinaryOperator.OR, true ),
            testcase( true, false, EBinaryOperator.OR, true ),
            testcase( false, true, EBinaryOperator.OR, true ),
            testcase( false, false, EBinaryOperator.OR, false ),

            testcase( true, true, EBinaryOperator.AND, true ),
            testcase( true, false, EBinaryOperator.AND, false ),
            testcase( false, true, EBinaryOperator.AND, false ),
            testcase( false, false, EBinaryOperator.AND, false ),

            testcase( true, true, EBinaryOperator.XOR, false ),
            testcase( true, false, EBinaryOperator.XOR, true ),
            testcase( false, true, EBinaryOperator.XOR, true ),
            testcase( false, false, EBinaryOperator.XOR, false )

        ).toArray();
    }

    /**
     * test-case generator
     *
     * @param p_lhs left-hand-side argument
     * @param p_rhs right-hand-side argument
     * @param p_operator operator
     * @param p_result result
     * @return test-case
     */
    private static Object testcase( @Nonnull final Object p_lhs, @Nonnull final Object p_rhs,
                                    @Nonnull final EBinaryOperator p_operator, @Nonnull final Object p_result )
    {
        return Stream.of( p_lhs, p_rhs, p_operator, p_result ).toArray();
    }

    /**
     * test assignment operator with variables
     * @param p_data input data
     */
    @Test
    @SuppressWarnings( "unchecked" )
    @UseDataProvider( "operator" )
    public final void variable( @Nonnull final Object[] p_data )
    {
        Assume.assumeTrue( p_data.length == 4 );

        final List<ITerm> l_return = new ArrayList<>();

        final IVariable<Object> l_lhs = new CVariable<>( "Lhs" );
        final IVariable<Object> l_rhs = new CVariable<>( "Rhs" );

        l_lhs.set( p_data[0] );
        l_rhs.set( p_data[1] );

        Assert.assertTrue(
            new CBinaryExpression(
                (EBinaryOperator) p_data[2],
                new CPassVariable( l_lhs ),
                new CPassVariable( l_rhs )
            ).execute(
                false,
                new CLocalContext( l_lhs, l_rhs ),
                Collections.emptyList(),
                l_return
            ).value()
        );

        Assert.assertEquals( 1, l_return.size() );
        Assert.assertEquals( p_data[0], l_lhs.raw() );
        Assert.assertEquals( p_data[1],  l_rhs.raw() );
        Assert.assertEquals( p_data[3], l_return.get( 0 ).raw() );
    }

    /**
     * test assignment operator with native data
     * @param p_data input data
     */
    @Test
    @SuppressWarnings( "unchecked" )
    @UseDataProvider( "operator" )
    public final void raw( @Nonnull final Object[] p_data )
    {
        Assume.assumeTrue( p_data.length == 4 );

        final List<ITerm> l_return = new ArrayList<>();

        Assert.assertTrue(
            new CBinaryExpression(
                (EBinaryOperator) p_data[2],
                new CPassRaw<>( p_data[0] ),
                new CPassRaw<>( p_data[1] )
            ).execute(
                false,
                IContext.EMPTYPLAN,
                Collections.emptyList(),
                l_return
            ).value()
        );

        Assert.assertEquals( 1, l_return.size() );
        Assert.assertEquals( p_data[3], l_return.get( 0 ).raw() );
    }

}

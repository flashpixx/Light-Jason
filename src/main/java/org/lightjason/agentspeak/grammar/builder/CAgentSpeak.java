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

package org.lightjason.agentspeak.grammar.builder;

import com.codepoetics.protonpack.StreamUtils;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.lightjason.agentspeak.action.IAction;
import org.lightjason.agentspeak.common.CCommon;
import org.lightjason.agentspeak.common.CPath;
import org.lightjason.agentspeak.common.IPath;
import org.lightjason.agentspeak.error.CIllegalArgumentException;
import org.lightjason.agentspeak.language.ILiteral;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.execution.IExecution;
import org.lightjason.agentspeak.language.execution.action.CActionProxy;
import org.lightjason.agentspeak.language.execution.action.CBeliefAction;
import org.lightjason.agentspeak.language.execution.action.CDeconstruct;
import org.lightjason.agentspeak.language.execution.action.CMultiAssignment;
import org.lightjason.agentspeak.language.execution.action.CRepair;
import org.lightjason.agentspeak.language.execution.action.CSingleAssignment;
import org.lightjason.agentspeak.language.execution.action.CTernaryOperation;
import org.lightjason.agentspeak.language.execution.action.achievement_test.CTestGoal;
import org.lightjason.agentspeak.language.execution.action.achievement_test.CTestRule;
import org.lightjason.agentspeak.language.execution.action.unify.CDefaultUnify;
import org.lightjason.agentspeak.language.execution.action.unify.CExpressionUnify;
import org.lightjason.agentspeak.language.execution.action.unify.CVariableUnify;
import org.lightjason.agentspeak.language.execution.expression.IExpression;
import org.lightjason.agentspeak.language.execution.expressionunary.CDecrement;
import org.lightjason.agentspeak.language.execution.expressionunary.CIncrement;
import org.lightjason.agentspeak.language.instantiable.plan.IPlan;
import org.lightjason.agentspeak.language.instantiable.plan.annotation.CAtomAnnotation;
import org.lightjason.agentspeak.language.instantiable.plan.annotation.CValueAnnotation;
import org.lightjason.agentspeak.language.instantiable.plan.annotation.IAnnotation;
import org.lightjason.agentspeak.language.instantiable.plan.trigger.ITrigger;
import org.lightjason.agentspeak.language.instantiable.rule.CRulePlaceholder;
import org.lightjason.agentspeak.language.instantiable.rule.IRule;
import org.lightjason.agentspeak.language.variable.IVariable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


/**
 * builder of agentspeak components
 */
public final class CAgentSpeak
{
    /**
     * reg pattern for matching annotation constant values
     */
    private static final Pattern ANNOTATIONCONSTANT = Pattern.compile( ".+\\( .+, .+ \\)" );

    /**
     * ctor
     */
    private CAgentSpeak()
    {

    }

    /**
     * build a plan object
     *
     * @param p_trigger trigger terminal
     * @return plan
     */
    @Nonnull
    public static IPlan plan( @Nonnull final TerminalNode p_trigger, @Nonnull final Stream<TerminalNode> p_annotation )
    {
        p_annotation.map( CAgentSpeak::annotation ).filter( Objects::nonNull );

        switch ( ITrigger.EType.from( p_trigger.getText() ) )
        {
            default:
                return IPlan.EMPTY;
        }
    }

    /**
     * build a rule
     *
     * @param p_literal literal
     * @param p_annotation annotation stream
     * @return rule
     */
    @Nonnull
    public static IRule rule( @Nonnull final ILiteral p_literal, @Nonnull final Stream<TerminalNode> p_annotation )
    {
        return IRule.EMPTY;
    }

    /**
     * create a rule placeholder
     *
     * @param p_literal literal
     * @return place holder rule
     */
    @Nonnull
    public static IRule ruleplaceholder( @Nonnull final ILiteral p_literal )
    {
        return new CRulePlaceholder(
            p_literal
        );
    }

    /**
     * build annotation object
     * @param p_annotation annotation terminal
     * @return null or annoation
     */
    @Nullable
    public static IAnnotation<?> annotation( @Nullable final TerminalNode p_annotation )
    {
        if ( Objects.isNull( p_annotation ) )
            return null;

        if ( p_annotation.getText().contains( "parallel" ) )
            return new CAtomAnnotation<>( IAnnotation.EType.PARALLEL );

        if ( p_annotation.getText().contains( "atomic" ) )
            return new CAtomAnnotation<>( IAnnotation.EType.ATOMIC );

        // on a constant annoation object, the data and name must be split at the comma,
        // the value can be a number value or a string
        if ( p_annotation.getText().contains( "constant" ) )
        {
            final Matcher l_match = ANNOTATIONCONSTANT.matcher( p_annotation.getText() );
            if ( !l_match.find() )
                return null;

            final String l_data = l_match.group( 2 ).replace( "'", "" ).replace( "\"", "" );
            try
            {
                return new CValueAnnotation<>( IAnnotation.EType.CONSTANT, l_match.group( 1 ), CRaw.numbervalue( l_data ) );
            }
            catch ( final NumberFormatException l_exception )
            {
                return new CValueAnnotation<>( IAnnotation.EType.CONSTANT, l_match.group( 1 ), l_data );
            }
        }

        return null;
    }

    /**
     * builds a repair chain
     *
     * @param p_chain input chain elements
     * @return null or repair
     */
    @Nullable
    public static IExecution repair( @Nonnull final Stream<IExecution> p_chain )
    {
        return new CRepair( p_chain );
    }

    /**
     * create a test-goal
     *
     * @param p_dollar dollar sign (rule / plan)
     * @param p_atom atom terminal
     * @return execution
     */
    @Nonnull
    public static IExecution testgoal( @Nullable final TerminalNode p_dollar, @Nonnull final TerminalNode p_atom )
    {
        return Objects.nonNull( p_dollar )
            ? new CTestRule( CPath.from( p_atom.getText() ) )
            : new CTestGoal( CPath.from( p_atom.getText() ) );
    }

    /**
     * build an unary expression
     *
     * @param p_operator operator
     * @param p_variable variable
     * @return null or execution
     */
    @Nullable
    public static IExecution unary( @Nonnull final TerminalNode p_operator, @Nonnull final IVariable<Number> p_variable )
    {
        switch ( p_operator.getText() )
        {
            case "++":
                return new CIncrement<>( p_variable );

            case "--":
                return new CDecrement<>( p_variable );

            default:
                return null;
        }
    }

    /**
     * build single assignment
     *
     * @param p_variable variable
     * @param p_execution execution structure
     * @return assignment execution
     */
    @Nonnull
    public static IExecution singleassignment( @Nonnull final IVariable<?> p_variable, @Nonnull final IExecution p_execution )
    {
        return new CSingleAssignment<>(
            p_variable,
            p_execution
        );
    }

    /**
     * build multi assignment
     *
     * @param p_variable variable
     * @param p_execution execution structure
     * @return assignment execution
     */
    @Nonnull
    public static IExecution multiassignment( @Nonnull final Stream<IVariable<?>> p_variable, @Nonnull final IExecution p_execution )
    {
        return new CMultiAssignment<>(
            p_variable,
            p_execution
        );
    }

    /**
     * build ternary operator
     *
     * @param p_expression expression definition
     * @param p_true true execution
     * @param p_false false execution
     * @return ternary operator
     */
    @Nonnull
    public static IExecution ternary( @Nonnull final IExpression p_expression, @Nonnull final IExecution p_true, @Nonnull final IExecution p_false )
    {
        return new CTernaryOperation(
            p_expression,
            p_true,
            p_false
        );
    }

    /**
     * build belief action
     *
     * @param p_addbelief add-belief terminal
     * @param p_deletebelief delete-belief terminal
     * @param p_literal belief literal
     * @return null or execution
     */
    @Nullable
    public static IExecution beliefaction( @Nullable final TerminalNode p_addbelief, @Nullable final TerminalNode p_deletebelief, @Nonnull final ILiteral p_literal )
    {
        if ( Objects.nonNull( p_addbelief ) )
            return new CBeliefAction( p_literal, CBeliefAction.EAction.ADD );

        if ( Objects.nonNull( p_deletebelief ) )
            return new CBeliefAction( p_literal, CBeliefAction.EAction.DELETE );

        return null;
    }

    /**
     * build deconstruct
     *
     * @return deconstruct execution
     */
    @Nonnull
    public static IExecution deconstruct( @Nonnull final ParseTreeVisitor<?> p_visitor, @Nonnull final Stream<IVariable<?>> p_variables,
                                          @Nullable final ITerm p_literal, @Nullable final ITerm p_variable )
    {
        return new CDeconstruct<>(
            p_variables,
            Objects.nonNull( p_literal ) ? p_literal : p_variable
        );
    }

    /**
     * creates an action execution definition
     *
     * @param p_actionliteral action literal
     * @param p_actions map with actions
     * @return wrapped action
     */
    @Nonnull
    public static IExecution action( @Nonnull final ILiteral p_actionliteral, @Nonnull final Map<IPath, IAction> p_actions )
    {
        final IAction l_action = p_actions.get( p_actionliteral.fqnfunctor() );
        if ( Objects.isNull( l_action ) )
            throw new CIllegalArgumentException( CCommon.languagestring( CAgentSpeak.class, "actionunknown", p_actionliteral ) );

        if ( p_actionliteral.orderedvalues().count() < l_action.minimalArgumentNumber() )
            throw new CIllegalArgumentException( CCommon.languagestring( CAgentSpeak.class, "argumentnumber", p_actionliteral, l_action.minimalArgumentNumber() ) );

        return new CActionProxy( p_actionliteral.hasAt(), l_action );
    }

    /**
     * unification
     *
     * @param p_parallel parallel call
     * @param p_literal literal call
     * @param p_constraint unficiation constraint
     * @return unification execution
     */
    @Nonnull
    public static IExecution unification( boolean p_parallel, @Nonnull final ILiteral p_literal, @Nullable final ITerm p_constraint )
    {
        if ( p_constraint instanceof IExpression )
            return new CExpressionUnify(
                p_parallel,
                p_literal,
                p_constraint.raw()
            );

        if ( p_constraint instanceof IVariable<?> )
            return new CVariableUnify(
                p_parallel,
                p_literal,
                p_constraint.raw()
            );

        return new CDefaultUnify( p_parallel, p_literal );
    }

}

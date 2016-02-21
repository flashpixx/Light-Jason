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

package lightjason.language;

import com.google.common.hash.Funnel;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.hash.PrimitiveSink;
import com.google.common.reflect.ClassPath;
import lightjason.agent.action.IAction;
import lightjason.error.CIllegalArgumentException;
import lightjason.language.execution.IContext;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * common structure for execution definition
 */
public final class CCommon
{
    /**
     * private ctor - avoid instantiation
     */
    private CCommon()
    {
    }

    /**
     * get all classes within an Java package as action
     *
     * @param p_package full-qualified package name
     * @return action set
     *
     * @todo can be moved to an own class
     */
    @SuppressWarnings( "unchecked" )
    public static Set<IAction> getActionsFromPackage( final String p_package ) throws IOException
    {
        return ClassPath.from( Thread.currentThread().getContextClassLoader() ).getTopLevelClassesRecursive( p_package ).parallelStream().map( i -> {

            try
            {
                final Class<?> l_class = i.load();
                if ( ( !Modifier.isAbstract( l_class.getModifiers() ) ) && ( !Modifier.isInterface( l_class.getModifiers() ) ) &&
                     ( Modifier.isPublic( l_class.getModifiers() ) ) && ( IAction.class.isAssignableFrom( l_class ) ) )
                    return (IAction) l_class.newInstance();
            }
            catch ( final IllegalAccessException | InstantiationException p_exception )
            {
            }

            return null;
        } ).filter( i -> i != null ).collect( Collectors.toSet() );
    }

    /**
     * returns a native / raw value of a term
     *
     * @param p_value any value type
     * @return term value or raw value
     */
    @SuppressWarnings( "unchecked" )
    public static <T, N> T getRawValue( final N p_value )
    {
        if ( p_value instanceof IVariable<?> )
            return ( (IVariable<?>) p_value ).getTyped();
        if ( p_value instanceof CRawTerm<?> )
            return ( (CRawTerm<?>) p_value ).getTyped();

        return (T) p_value;
    }

    /**
     * checks a term value for assignable class
     *
     * @param p_value any value type
     * @param p_class assignable class
     * @return term value or raw value
     */
    @SuppressWarnings( "unchecked" )
    public static <T> boolean isRawValueAssignableTo( final T p_value, final Class<?>... p_class )
    {
        if ( p_value instanceof IVariable<?> )
            return ( (IVariable<?>) p_value ).isValueAssignableTo( p_class );
        if ( p_value instanceof CRawTerm<?> )
            return ( (CRawTerm<?>) p_value ).isValueAssignableTo( p_class );

        return Arrays.asList( p_class ).stream().map( i -> i.isAssignableFrom( p_value.getClass() ) ).anyMatch( i -> i );
    }


    /**
     * replace variables with context variables
     *
     * @param p_context execution context
     * @param p_terms replacing term list
     * @return result term list
     */
    public static List<ITerm> replaceFromContext( final IContext<?> p_context, final Collection<? extends ITerm> p_terms )
    {
        return p_terms.stream().map( i -> replaceFromContext( p_context, i ) ).collect( Collectors.toList() );
    }

    /**
     * replace variable with context variable
     * other values will be passed
     *
     * @param p_context execution context
     * @param p_term term
     * @return replaces variable object
     */
    public static ITerm replaceFromContext( final IContext<?> p_context, final ITerm p_term )
    {
        if ( !( p_term instanceof IVariable<?> ) )
            return p_term;

        final IVariable<?> l_variable = p_context.getInstanceVariables().get( p_term.getFQNFunctor() );
        if ( l_variable != null )
            return l_variable;

        throw new CIllegalArgumentException(
                lightjason.common.CCommon.getLanguageString( CCommon.class, "variablenotfoundincontext", p_term.getFQNFunctor() )
        );
    }


    /**
     * flat term map into flat term list
     *
     * @param p_terms term collection
     * @return flat term map
     */
    public static List<ITerm> flatList( final List<? extends ITerm> p_terms )
    {
        return flattenToStream( p_terms ).collect( Collectors.toList() );
    }

    /**
     * flats and concat the term list
     *
     * @param p_input input term list
     * @return byte sequence with UTF-8 encoding
     *
     * @throws UnsupportedEncodingException is thrown on wrong encoding type
     */
    public static byte[] getBytes( final List<ITerm> p_input ) throws UnsupportedEncodingException
    {
        final StringBuilder l_result = new StringBuilder();
        ( flatList( p_input ) ).stream().forEach( i -> l_result.append( getRawValue( i ).toString() ) );
        return l_result.toString().getBytes( "UTF-8" );
    }

    /**
     * returns the hasing function for term data
     *
     * @return hasher
     */
    public static Hasher getTermHashing()
    {
        return Hashing.murmur3_32().newHasher();
    }

    /**
     *
     */
    public static <T extends Serializable> Funnel<T> getTermFunnel()
    {
        return new Funnel<T>()
        {
            @Override
            public final void funnel( final T p_object, final PrimitiveSink p_sink )
            {
                p_sink.putBytes( SerializationUtils.serialize( p_object ) );
            }
        };
    }

    /*
     * recursive flattering of a list structure
     *
     * @param p_list any collection type
     * @return term stream
     */
    @SuppressWarnings( "unchecked" )
    private static Stream<ITerm> flattenToStream( final Collection<?> p_list )
    {
        return p_list.stream().flatMap(
                i -> {
                    final Object l_value = getRawValue( i );
                    return l_value instanceof Collection<?>
                           ? flattenToStream( (List<?>) l_value )
                           : Stream.of( CRawTerm.from( l_value ) );

                } );
    }
}

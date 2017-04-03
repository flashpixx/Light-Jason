/*
 * @cond LICENSE
 * ######################################################################################
 * # LGPL License                                                                       #
 * #                                                                                    #
 * # This file is part of the LightJason AgentSpeak(L++)                                #
 * # Copyright (c) 2015-16, LightJason (info@lightjason.org)                            #
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

package org.lightjason.agentspeak.action.buildin.crypto;

import com.google.common.hash.Hashing;
import org.lightjason.agentspeak.action.buildin.IBuildinAction;
import org.lightjason.agentspeak.error.CRuntimeException;
import org.lightjason.agentspeak.language.CCommon;
import org.lightjason.agentspeak.language.CRawTerm;
import org.lightjason.agentspeak.language.ITerm;
import org.lightjason.agentspeak.language.execution.IContext;
import org.lightjason.agentspeak.language.execution.fuzzy.CFuzzyValue;
import org.lightjason.agentspeak.language.execution.fuzzy.IFuzzyValue;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;


/**
 * hash algorithm.
 * The actions creates a hash values of datasets, the first argument is the name of the hasing algorithm
 * (Adler-32, CRC-32, CRC-32C, Murmur3-32, Murmur3-128, Siphash-2-4, MD2, MD5, SHA-1, SHA-224, SHA-256, SHA-384, SHA-512),
 * for all other unflatten arguments a hash value is calculated and the action returns the hash values back and never fails
 *
 * @code [Hash1 | Hash2 | Hash3] = crypto/hash( "Adler-32 | CRC-32 | CRC-32C | ...", Dataset1, Dataset2, Dataset3 ); @endcode
 * @see https://en.wikipedia.org/wiki/Secure_Hash_Algorithm
 * @see https://en.wikipedia.org/wiki/MD2_(cryptography)
 * @see https://en.wikipedia.org/wiki/MD5
 * @see https://en.wikipedia.org/wiki/Adler-32
 * @see https://en.wikipedia.org/wiki/Cyclic_redundancy_check
 * @see https://en.wikipedia.org/wiki/MurmurHash
 * @see https://en.wikipedia.org/wiki/SipHash
 * @see http://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#MessageDigest
 * @see https://github.com/google/guava/wiki/HashingExplained
 */
public final class CHash extends IBuildinAction
{

    @Override
    public final int minimalArgumentNumber()
    {
        return 2;
    }

    @Override
    public final IFuzzyValue<Boolean> execute( final IContext p_context, final boolean p_parallel, final List<ITerm> p_argument, final List<ITerm> p_return,
                                               final List<ITerm> p_annotation
    )
    {
        CCommon.flatcollection( p_argument )
               .skip( 1 )
               .map( ITerm::raw )
               .map( i -> hash( p_context, p_argument.get( 0 ).<String>raw(), bytes( p_context, i ) ) )
               .map( CRawTerm::from )
               .forEach( p_return::add );

        return CFuzzyValue.from( true );
    }

    /**
     * converts an object to a byte array
     *
     * @param p_context execution context
     * @param p_object object
     * @return byte array
     */
    private static byte[] bytes( final IContext p_context, final Object p_object )
    {
        try
        {
            return CCommon.getBytes( Stream.of( p_object ) );
        }
        catch ( final IOException l_exception )
        {
            throw new CRuntimeException( l_exception, p_context );
        }
    }


    /**
     * runs hashing function with difference between Google Guava hashing and Java default digest
     *
     * @param p_context execution context
     * @param p_algorithm algorithm name
     * @param p_data byte data representation
     * @return hash value
     */
    private static String hash( final IContext p_context, final String p_algorithm, final byte[] p_data )
    {
        switch ( p_algorithm.trim().toLowerCase( Locale.ROOT ) )
        {
            case "adler-32":
                return Hashing.adler32().newHasher().putBytes( p_data ).hash().toString();

            case "crc-32":
                return Hashing.crc32().newHasher().putBytes( p_data ).hash().toString();

            case "crc-32c":
                return Hashing.crc32c().newHasher().putBytes( p_data ).hash().toString();

            case "murmur3-32":
                return Hashing.murmur3_32().newHasher().putBytes( p_data ).hash().toString();

            case "murmur3-128":
                return Hashing.murmur3_128().newHasher().putBytes( p_data ).hash().toString();

            case "siphash-2-4":
                return Hashing.sipHash24().newHasher().putBytes( p_data ).hash().toString();

            default:
                try
                {
                    return String.format( "%032x", new BigInteger( 1, MessageDigest.getInstance( p_algorithm ).digest( p_data ) ) );
                }
                catch ( final NoSuchAlgorithmException l_exception )
                {
                    throw new CRuntimeException( l_exception, p_context );
                }
        }
    }

}

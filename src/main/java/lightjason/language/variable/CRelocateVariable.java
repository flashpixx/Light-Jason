package lightjason.language.variable;

import lightjason.common.CPath;
import lightjason.common.IPath;


/**
 * class for a relocated variable
 *
 * @tparam T variable type
 */
public final class CRelocateVariable<T> extends CVariable<T> implements IRelocateVariable
{
    /**
     * reference to relocated variable
     */
    private final IVariable<?> m_relocate;

    /**
     * ctor
     *
     * @param p_functor original functor of the variable, which should be used
     * @param p_variable variable which should be relocated
     */
    public CRelocateVariable( final IPath p_functor, final IVariable<?> p_variable )
    {
        super( p_functor );
        m_relocate = p_variable;
    }

    /**
     * private ctor for creating object-copy
     *
     * @param p_functor functor
     * @param p_value value
     * @param p_variable referenced variable
     */
    private CRelocateVariable( final IPath p_functor, final T p_value, final IVariable<?> p_variable )
    {
        super( p_functor, p_value );
        m_relocate = p_variable;
    }

    @Override
    public final IVariable<?> relocate()
    {
        return m_relocate.set( this.getTyped() );
    }

    @Override
    public final IVariable<T> shallowcopy( final IPath... p_prefix )
    {
        return ( p_prefix == null ) || ( p_prefix.length == 0 )
               ? new CRelocateVariable<T>( m_functor, m_value, m_relocate )
               : new CRelocateVariable<T>( p_prefix[0].append( m_functor ), m_value, m_relocate );
    }

    @Override
    public final IVariable<T> shallowcopySuffix()
    {
        return new CRelocateVariable<>( CPath.from( m_functor.getSuffix() ), m_relocate );
    }
}

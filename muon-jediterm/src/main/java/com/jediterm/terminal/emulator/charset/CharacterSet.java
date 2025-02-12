package com.jediterm.terminal.emulator.charset;

/**
 * Provides an enum with names for the supported character sets.
 */
public enum CharacterSet
{
  ASCII( 'B' )
    {
      @Override
      public int map( int index )
      {
        return -1;
      }
    },
  BRITISH( 'A' )
    {
      @Override
      public int map( int index )
      {
        if ( index == 3 )
        {
          // Pound sign...
          return '£';
        }
        return -1;
      }
    },
  DANISH( 'E', '6' )
    {
      @Override
      public int map( int index )
      {
        switch ( index )
        {
          case 32:
            return 'Ä';
          case 59:
            return 'Æ';
          case 60:
            return 'Ø';
          case 61:
            return 'Å';
          case 62:
            return 'Ü';
          case 64:
            return 'ä';
          case 91:
            return 'æ';
          case 92:
            return 'ø';
          case 93:
            return 'å';
          case 94:
            return 'ü';
          default:
            return -1;
        }
      }
    },
  DEC_SPECIAL_GRAPHICS( '0', '2' )
    {
      @Override
      public int map( int index )
      {
        if ( index >= 64 && index < 96 )
        {
          return (Character) CharacterSets.DEC_SPECIAL_CHARS[index - 64][0];
        }
        return -1;
      }
    },
  DEC_SUPPLEMENTAL( 'U', '<' )
    {
      @Override
      public int map( int index )
      {
        if ( index >= 0 && index < 64 )
        {
          // Set the 8th bit...
          return index + 160;
        }
        return -1;
      }
    },
  DUTCH( '4' )
    {
      @Override
      public int map( int index )
      {
        switch ( index )
        {
          case 3:
            return '£';
          case 32:
            return '¾';
          case 59:
            return 'ĳ';
          case 60:
            return '½';
          case 61:
            return '|';
          case 91:
            return '¨';
          case 92:
            return 'ƒ';
          case 93:
            return '¼';
          case 94:
            return '´';
          default:
            return -1;
        }
      }
    },
  FINNISH( 'C', '5' )
    {
      @Override
      public int map( int index )
      {
        switch ( index )
        {
          case 59:
            return 'Ä';
          case 60:
            return 'Ô';
          case 61:
            return 'Å';
          case 62:
            return 'Ü';
          case 64:
            return 'é';
          case 91:
            return 'ä';
          case 92:
            return 'ö';
          case 93:
            return 'å';
          case 94:
            return 'ü';
          default:
            return -1;
        }
      }
    },
  FRENCH( 'R' )
    {
      @Override
      public int map( int index )
      {
        switch ( index )
        {
          case 3:
            return '£';
          case 32:
            return 'à';
          case 59:
            return '°';
          case 60:
            return 'ç';
          case 61:
            return '¦';
          case 91:
            return 'é';
          case 92:
            return 'ù';
          case 93:
            return 'è';
          case 94:
            return '¨';
          default:
            return -1;
        }
      }
    },
  FRENCH_CANADIAN( 'Q' )
    {
      @Override
      public int map( int index )
      {
        switch ( index )
        {
          case 32:
            return 'à';
          case 59:
            return 'â';
          case 60:
            return 'ç';
          case 61:
            return 'ê';
          case 62:
            return 'î';
          case 91:
            return 'é';
          case 92:
            return 'ù';
          case 93:
            return 'è';
          case 94:
            return 'û';
          default:
            return -1;
        }
      }
    },
  GERMAN( 'K' )
    {
      @Override
      public int map( int index )
      {
        switch ( index )
        {
          case 32:
            return '§';
          case 59:
            return 'Ä';
          case 60:
            return 'Ö';
          case 61:
            return 'Ü';
          case 91:
            return 'ä';
          case 92:
            return 'ö';
          case 93:
            return 'ü';
          case 94:
            return 'ß';
          default:
            return -1;
        }
      }
    },
  ITALIAN( 'Y' )
    {
      @Override
      public int map( int index )
      {
        switch ( index )
        {
          case 3:
            return '£';
          case 32:
            return '§';
          case 59:
            return 'º';
          case 60:
            return 'ç';
          case 61:
            return 'é';
          case 91:
            return 'à';
          case 92:
            return 'ò';
          case 93:
            return 'è';
          case 94:
            return 'ì';
          default:
            return -1;
        }
      }
    },
  SPANISH( 'Z' )
    {
      @Override
      public int map( int index )
      {
        switch ( index )
        {
          case 3:
            return '£';
          case 32:
            return '§';
          case 59:
            return '¡';
          case 60:
            return 'Ñ';
          case 61:
            return '¿';
          case 91:
            return '°';
          case 92:
            return 'ñ';
          case 93:
            return 'ç';
          default:
            return -1;
        }
      }
    },
  SWEDISH( 'H', '7' )
    {
      @Override
      public int map( int index )
      {
        switch ( index )
        {
          case 32:
            return 'É';
          case 59:
            return 'Ä';
          case 60:
            return 'Ö';
          case 61:
            return 'Å';
          case 62:
            return 'Ü';
          case 64:
            return 'é';
          case 91:
            return 'ä';
          case 92:
            return 'ö';
          case 93:
            return 'å';
          case 94:
            return 'ü';
          default:
            return -1;
        }
      }
    },
  SWISS( '=' )
    {
      @Override
      public int map( int index )
      {
        switch ( index )
        {
          case 3:
            return 'ù';
          case 32:
            return 'à';
          case 59:
            return 'é';
          case 60:
            return 'ç';
          case 61:
            return 'ê';
          case 62:
            return 'î';
          case 63:
            return 'è';
          case 64:
            return 'ô';
          case 91:
            return 'ä';
          case 92:
            return 'ö';
          case 93:
            return 'ü';
          case 94:
            return 'û';
          default:
            return -1;
        }
      }
    };

  private final int[] myDesignations;

  /**
   * Creates a new {@link CharacterSet} instance.
   *
   * @param designations
   *          the characters that designate this character set, cannot be
   *          <code>null</code>.
   */
  CharacterSet(int... designations)
  {
    myDesignations = designations;
  }

  // METHODS

  /**
   * Returns the {@link CharacterSet} for the given character.
   *
   * @param designation
   *          the character to translate to a {@link CharacterSet}.
   * @return a character set name corresponding to the given character,
   *         defaulting to ASCII if no mapping could be made.
   */
  public static CharacterSet valueOf( char designation )
  {
    for ( CharacterSet csn : values() )
    {
      if ( csn.isDesignation( designation ) )
      {
        return csn;
      }
    }
    return ASCII;
  }

  /**
   * Maps the character with the given index to a character in this character
   * set.
   *
   * @param index
   *          the index of the character set, >= 0 && < 128.
   * @return a mapped character, or -1 if no mapping could be made and the
   *         ASCII value should be used.
   */
  public abstract int map( int index );

  /**
   * Returns whether or not the given designation character belongs to this
   * character set's set of designations.
   *
   * @param designation
   *          the designation to test for.
   * @return <code>true</code> if the given designation character maps to this
   *         character set, <code>false</code> otherwise.
   */
  private boolean isDesignation( char designation )
  {
    for (int myDesignation : myDesignations) {
      if (myDesignation == designation) {
        return true;
      }
    }
    return false;
  }
}

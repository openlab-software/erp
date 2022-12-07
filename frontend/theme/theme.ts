import { extendTheme } from '@chakra-ui/react';
import { mode } from '@chakra-ui/theme-tools';

import LayoutComponents from './additions/layout';

const fonts = { mono: `'Menlo', monospace` }

const breakpoints = {
  sm: '40em',
  md: '52em',
  lg: '64em',
  xl: '80em',
}

const theme = extendTheme({
  globalStyles: {
    colors: {
      gray: {
        700: '#1f2733'
      }
    },
  },
  styles: {
    global: (props: any) => ({
      body: {
        bg: mode('gray.50', 'gray.800')(props),
        fontFamily: "'Roboto', sans-serif"
      },
      html: {
        fontFamily: "'Roboto', sans-serif"
      }
    })
  },
  semanticTokens: {
    colors: {
      text: {
        default: '#16161D',
        _dark: '#ade3b8',
      },
      heroGradientStart: {
        default: '#7928CA',
        _dark: '#e3a7f9',
      },
      heroGradientEnd: {
        default: '#FF0080',
        _dark: '#fbec8f',
      },
    },
    radii: {
      button: '12px',
    },
  },
  colors: {
    black: '#16161D',
  },
  fonts,
  breakpoints,
  components: {
    ...LayoutComponents
  }
})

export type variants = 'main';

export default theme
import { Box, BoxProps, useStyleConfig } from '@chakra-ui/react';

import { variants } from '../../../theme/theme';
import MainBoxContainer from './MainBoxContainer';
import MainBoxContent from './MainBoxContent';

type MainBoxProps = React.PropsWithChildren & BoxProps & { variant?: variants }

type MainBoxFC = React.FC<MainBoxProps> & {
    Container: React.FC<BoxProps & { variant?: variants }>
    Content: React.FC<BoxProps & { variant?: variants }>
}

const MainBox: MainBoxFC = ({ variant, children, ...rest }) => {
    const styles = useStyleConfig("MainBox", { variant });

    return (
        <Box __css={styles} {...rest}>
            {children}
        </Box>
    );
}

MainBox.Container = MainBoxContainer;
MainBox.Content = MainBoxContent;

export default MainBox;
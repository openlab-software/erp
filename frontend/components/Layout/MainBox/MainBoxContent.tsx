import { Box, BoxProps, useStyleConfig } from '@chakra-ui/react';

import { variants } from '../../../theme/theme';

const MainBoxContent: React.FC<BoxProps & { variant?: variants }> = ({ variant, children, ...rest }) => {
    const styles = useStyleConfig("MainBoxContent", { variant });
    return (
        <Box __css={styles} {...rest}>
            {children}
        </Box>
    );
}

export default MainBoxContent;
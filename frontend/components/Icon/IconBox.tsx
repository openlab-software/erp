import { Flex, FlexProps } from '@chakra-ui/react';
import React from 'react';

type IconBoxProps = FlexProps;

const IconBox: React.FC<IconBoxProps> = ({ children, ...rest }) => {
    return (
        <Flex
            alignItems="center"
            justifyContent="center"
            borderRadius="12px"
            {...rest}
        >
            {children}
        </Flex>
    );
}

export default IconBox;
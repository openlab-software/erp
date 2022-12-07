import { Text, useColorModeValue } from '@chakra-ui/react';
import { Fragment } from 'react';

import SidebarLink, { SidebarLinkProps } from './SidebarLink';

const SidebarLinkCategory: React.FC<SidebarLinkProps> = ({ route }) => {
    const activeColor = useColorModeValue("gray.700", "white");

    return (
        <Fragment>
            <Text
                color={activeColor}
                fontWeight="bold"
                mb={{
                    xl: "12px",
                }}
                mx="auto"
                ps={{
                    sm: "10px",
                    xl: "16px",
                }}
                py="12px"
            >
                {route.name}
            </Text>
            {route.routes?.map(subroute => (
                <SidebarLink key={subroute.name} route={subroute} />
            ))}
        </Fragment>
    );
}

export default SidebarLinkCategory;
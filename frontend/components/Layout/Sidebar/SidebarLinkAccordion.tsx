import { ListItem, Text, useColorModeValue } from '@chakra-ui/react';
import Link from 'next/link';

import { RoutePath } from '../../../app/routesPaths';
import useRoutePathInfo from '../../../hooks/useRoutePathInfo';

type SidebarLinkAccordionProps = {
    route: RoutePath
};

const SidebarLinkAccordion: React.FC<SidebarLinkAccordionProps> = ({ route }) => {
    const activeColor = useColorModeValue("gray.700", "white");
    const inactiveColor = useColorModeValue("gray.400", "gray.400");
    const routePathInfo = useRoutePathInfo(route);

    return (
        <Link href={route.path}>
            <ListItem pt="5px" ms="26px">
                <Text
                    color={
                        routePathInfo.isActive
                            ? activeColor
                            : inactiveColor
                    }
                    fontWeight={
                        routePathInfo.isActive ? "bold" : "normal"
                    }
                    fontSize="sm"
                >
                    {route.name}
                </Text>
            </ListItem>
        </Link>
    );
}

export default SidebarLinkAccordion;
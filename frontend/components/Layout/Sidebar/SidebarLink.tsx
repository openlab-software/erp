import { Button, Flex, Icon, Text, useColorModeValue } from '@chakra-ui/react';
import React, { Fragment, ReactNode } from 'react';

import { RoutePath } from '../../../app/routesPaths';
import useRoutePathInfo from '../../../hooks/useRoutePathInfo';
import IconBox from '../../Icon/IconBox';
import SidebarLinkCollapse from './SidebarCollapse';
import SidebarLinkCategory from './SidebarLinkCategory';
import SidebarLinkSimple from './SidebarLinkSimple';

type SidebarLinkProps = {
    route: RoutePath
};

const SidebarLink: React.FC<SidebarLinkProps> = ({ route }) => {
    const activeBg = useColorModeValue("white", "gray.700");
    const inactiveBg = useColorModeValue("white", "gray.700");
    const activeColor = useColorModeValue("gray.700", "white");
    const inactiveColor = useColorModeValue("gray.400", "gray.400");
    const routePathInfo = useRoutePathInfo(route);

    return (
        <Fragment>
            {!!route.category && (<SidebarLinkCategory route={route} />)}
            {!!route.collapse && (<SidebarLinkCollapse route={route} />)}
            {!route.category && !route.collapse && (<SidebarLinkSimple route={route} />)}
        </Fragment>
    );
}

export default SidebarLink;
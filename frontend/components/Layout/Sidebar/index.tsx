import { Box, Link, Stack, Text } from '@chakra-ui/react';
import { useMemo, useRef } from 'react';

import routePaths, { RoutePath } from '../../../app/routesPaths';
import Scrollbar from '../../Scrollbar';
import Separator from '../../Separator';
import SidebarLink from './SidebarLink';
import SidebarLinkCategory from './SidebarLinkCategory';

type SidebarFC = React.FC & {
    Link: React.FC<{ route: RoutePath }>
    CategoryLink: React.FC<{ route: RoutePath }>
}


const Sidebar: SidebarFC = () => {
    const mainPanel = useRef(null);

    const sidebarContent = <>
        <Box pt={"25px"} mb="12px">
            <Link
                href={`${process.env.PUBLIC_URL}/`}
                display="flex"
                lineHeight="100%"
                mb="30px"
                fontWeight="bold"
                justifyContent="center"
                alignItems="center"
                fontSize="11px"
            >

                <Text fontSize="sm" mt="3px">
                    patrick.dev.br ERP
                </Text>
            </Link>
            <Separator />
        </Box>
        <Stack direction="column" mb="40px">
            <Box>
                {routePaths.map(route => <SidebarLink route={route} key={route.name} />)}
            </Box>
        </Stack>
    </>

    return (
        <Box ref={mainPanel}>
            <Box display={{ sm: "none", xl: "block" }} position="fixed">
                <Box
                    bg="none"
                    w="260px"
                    maxW="260px"
                    ms={{
                        sm: "16px",
                    }}
                    my={{
                        sm: "16px",
                    }}
                    h="calc(100vh - 32px)"
                    ps="20px"
                    pe="20px"
                >
                    <Scrollbar>{sidebarContent}</Scrollbar>
                </Box>
            </Box>
        </Box>
    )
}

Sidebar.Link = SidebarLink;
Sidebar.CategoryLink = SidebarLinkCategory;

export default Sidebar;
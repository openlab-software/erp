import { Box, HStack, Icon, ListItem, Text, useColorModeValue } from '@chakra-ui/react';
import Link from 'next/link';
import { FaCircle } from 'react-icons/fa';

import useRoutePathInfo from '../../../hooks/useRoutePathInfo';
import IconBox from '../../Icon/IconBox';
import { SidebarLinkProps } from './SidebarLink';

const SidebarLinkText: React.FC<SidebarLinkProps> = ({ route }) => {
    const activeColor = useColorModeValue("gray.700", "white");
    const inactiveColor = useColorModeValue("gray.400", "gray.400");

    const inactiveColorIcon = useColorModeValue("teal.300", "teal.300");
    const activeColorIcon = useColorModeValue("white", "white");
    const activeBg = useColorModeValue("teal.300", "teal.300");
    const inactiveBg = useColorModeValue("white", "gray.700");

    const routePathInfo = useRoutePathInfo(route);
    const RouteIcon =
        typeof route.icon === "string" ? (
            <Icon>{route.icon}</Icon>
        ) : (
            <Icon as={route.icon} />
        );
    return (
        <Link href={route.path}>
            {route.icon ? (
                <Box>
                    <HStack spacing="14px" py="15px" px="15px">
                        <IconBox
                            bg={routePathInfo.isActive ? activeBg : inactiveBg}
                            color={routePathInfo.isActive ? activeColorIcon : inactiveColorIcon}
                            h="30px"
                            w="30px"
                            transition={"0.2s linear"}
                        >
                            {RouteIcon}
                        </IconBox>
                        <Text
                            color={
                                routePathInfo.isActive
                                    ? activeColor
                                    : inactiveColor
                            }
                            fontWeight={routePathInfo.isActive ? "bold" : "normal"}
                            fontSize="sm"
                        >
                            {route.name}
                        </Text>
                    </HStack>
                </Box>
            ) : (
                <HStack
                    spacing={
                        "22px"
                    }
                    py="5px"
                    px="10px"
                >
                    <Icon
                        as={FaCircle}
                        w={routePathInfo.isActive ? "10px" : "6px"}
                        color="teal.300"
                        display={"block"}
                    />
                    <Text
                        color={
                            routePathInfo.isActive ? activeColor : inactiveColor
                        }
                        fontWeight={
                            routePathInfo.isActive ? "bold" : "normal"
                        }
                    >
                        {route.name}
                    </Text>
                </HStack>

            )}
        </Link>
    );
}

export default SidebarLinkText;
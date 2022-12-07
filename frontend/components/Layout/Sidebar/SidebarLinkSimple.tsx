import { PhoneIcon } from '@chakra-ui/icons';
import { Box, HStack, Icon, ListItem, Text, useColorModeValue } from '@chakra-ui/react';
import Link from 'next/link';
import { FaCircle } from 'react-icons/fa';

import { RoutePath } from '../../../app/routesPaths';
import useRoutePathInfo from '../../../hooks/useRoutePathInfo';
import IconBox from '../../Icon/IconBox';

type SidebarLinkSimpleProps = {
    route: RoutePath
};

const SidebarLinkSimple: React.FC<SidebarLinkSimpleProps> = ({ route }) => {
    const activeColor = useColorModeValue("gray.700", "white");
    const inactiveColor = useColorModeValue("gray.400", "gray.400");

    const routePathInfo = useRoutePathInfo(route);
    const RouteIcon = typeof route.icon === 'string' ? <Icon name={route.icon} /> : <PhoneIcon />;
    return (
        <Link href={route.path}>
            {route.icon ? (
                <Box>
                    <HStack spacing="14px" py="15px" px="15px">
                        <IconBox
                            bg="teal.300"
                            color="white"
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
                <ListItem>
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
                </ListItem>
            )}
        </Link>
    );
}

export default SidebarLinkSimple;
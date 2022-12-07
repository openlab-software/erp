import { PhoneIcon } from '@chakra-ui/icons';
import {
    Accordion,
    AccordionButton,
    AccordionIcon,
    AccordionItem,
    AccordionPanel,
    Button,
    Flex,
    HStack,
    Icon,
    Link,
    List,
    ListItem,
    Text,
    useColorModeValue,
} from '@chakra-ui/react';
import { FaCircle } from 'react-icons/fa';

import useRoutePathInfo from '../../../hooks/useRoutePathInfo';
import IconBox from '../../Icon/IconBox';
import SidebarLink, { SidebarLinkProps } from './SidebarLink';


const SidebarLinkCollapse: React.FC<SidebarLinkProps> = ({ route }) => {
    const activeBg = useColorModeValue("teal.300", "teal.300");
    const activeAccordionBg = useColorModeValue("white", "gray.700");
    const inactiveBg = useColorModeValue("white", "gray.700");
    const inactiveColorIcon = useColorModeValue("teal.300", "teal.300");
    const activeColorIcon = useColorModeValue("white", "white");
    const activeColor = useColorModeValue("gray.700", "white");
    const inactiveColor = useColorModeValue("gray.400", "gray.400");
    const sidebarActiveShadow = "0px 7px 11px rgba(0, 0, 0, 0.04)";

    const routePathInfo = useRoutePathInfo(route);
    const RouteIcon =
        typeof route.icon === "string" ? (
            <Icon>{route.icon}</Icon>
        ) : (
            <Icon as={route.icon} />
        );

    return (
        <Accordion allowToggle>
            <AccordionItem border="none">
                <AccordionButton
                    as='div'
                    display="flex"
                    alignItems='center'
                    justifyContent="center"
                    boxShadow={
                        routePathInfo.isActive && route.icon
                            ? '0px 7px 11px rgba(0, 0, 0, 0.04)'
                            : undefined
                    }
                    _hover={{
                        boxShadow:
                            routePathInfo.isActive && route.icon
                                ? '0px 7px 11px rgba(0, 0, 0, 0.04)'
                                : undefined
                    }}
                    _focus={{
                        boxShadow: "none",
                    }}
                    borderRadius="15px"
                    w={"100%"}
                    px={route.icon ? undefined : "0px"}
                    py={route.icon ? "12px" : undefined}
                    bg={
                        routePathInfo.isActive && route.icon
                            ? activeAccordionBg
                            : "transparent"
                    }
                >
                    {routePathInfo.isActive ? (
                        <Button
                            boxSize="initial"
                            justifyContent="flex-start"
                            alignItems="center"
                            bg="transparent"
                            transition='0.2s linear'
                            mx={{
                                xl: "auto",
                            }}
                            px="0px"
                            borderRadius="15px"
                            w="100%"
                            _hover={{
                                background: 'transparent'
                            }}
                            _active={{
                                bg: "inherit",
                                transform: "none",
                                borderColor: "transparent",
                                border: "none",
                            }}
                            _focus={{
                                transform: "none",
                                borderColor: "transparent",
                                border: "none",
                            }}
                        >
                            {route.icon ? (
                                <Flex>
                                    <IconBox
                                        bg={activeBg}
                                        color={activeColorIcon}
                                        h="30px"
                                        w="30px"
                                        me="12px"
                                        transition='0.2s linear'
                                    >
                                        {RouteIcon}
                                    </IconBox>
                                    <Text
                                        color={activeColor}
                                        my="auto"
                                        fontSize="sm"
                                        display="block"
                                    >
                                        {route.name}
                                    </Text>
                                </Flex>
                            ) : (
                                <HStack
                                    spacing="22px"
                                    ps='10px'
                                    ms='0px'
                                >
                                    <Icon
                                        as={FaCircle}
                                        w="10px"
                                        color="teal.300"
                                        display="block"
                                    />
                                    <Text color={activeColor} my="auto" fontSize="sm">
                                        {route.name}
                                    </Text>
                                </HStack>
                            )}
                        </Button>
                    ) : (
                        <Button
                            boxSize="initial"
                            justifyContent="flex-start"
                            alignItems="center"
                            bg="transparent"
                            mx={{
                                xl: "auto",
                            }}
                            px="0px"
                            borderRadius="15px"
                            w="100%"
                            _hover={{
                                bg: 'none'
                            }}
                            _active={{
                                bg: "inherit",
                                transform: "none",
                                borderColor: "transparent",
                            }}
                            _focus={{
                                borderColor: "transparent",
                                boxShadow: "none",
                            }}
                        >
                            {route.icon ? (
                                <Flex>
                                    <IconBox
                                        bg={inactiveBg}
                                        color={inactiveColorIcon}
                                        h="30px"
                                        w="30px"
                                        me="12px"
                                        transition='0.2s linear'
                                        boxShadow={sidebarActiveShadow}
                                        _hover={{ boxShadow: sidebarActiveShadow }}
                                    >
                                        {RouteIcon}
                                    </IconBox>
                                    <Text
                                        color={inactiveColor}
                                        my="auto"
                                        fontSize="sm"
                                        display="block"
                                    >
                                        {route.name}
                                    </Text>
                                </Flex>
                            ) : (
                                <HStack
                                    spacing="26px"
                                    ps='10px'
                                    ms='0px'
                                >
                                    <Icon
                                        as={FaCircle}
                                        w="6px"
                                        color="teal.300"
                                        display='block'
                                    />
                                    <Text
                                        color={inactiveColor}
                                        my="auto"
                                        fontSize="md"
                                        fontWeight="normal"
                                    >
                                        {route.name}
                                    </Text>
                                </HStack>
                            )}
                        </Button>
                    )}
                    <AccordionIcon
                        color="gray.400"
                        display='block'
                    />
                </AccordionButton>
                <AccordionPanel
                    pe={route.icon ? undefined : "0px"}
                    pb="8px"
                >
                    <List>
                        {
                            route.icon
                                ? <>{route.routes?.map(subroute => <SidebarLink route={subroute} key={route.name} />)}</>
                                : <>{route.routes?.map(subroute => <LinkAccordionItem route={subroute} key={route.name} />)}</>
                        }
                    </List>
                </AccordionPanel>
            </AccordionItem>
        </ Accordion>
    );
}

const LinkAccordionItem: React.FC<SidebarLinkProps> = ({ route }) => {
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

export default SidebarLinkCollapse;
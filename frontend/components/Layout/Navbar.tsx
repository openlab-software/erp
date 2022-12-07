import { Box, Breadcrumb, BreadcrumbItem, BreadcrumbLink, Flex } from '@chakra-ui/react';
import React from 'react';

const Navbar = () => {

    return (
        <Flex
            borderWidth="1.5px"
            borderStyle="solid"
            transitionDelay="0s, 0s, 0s, 0s"
            transitionDuration=" 0.25s, 0.25s, 0.25s, 0s"
            transition-property="box-shadow, background-color, filter, border"
            transitionTimingFunction="linear, linear, linear, linear"
            alignItems={{ xl: "center" }}
            borderRadius="16px"
            display="flex"
            minH="75px"
            justifyContent={{ xl: "center" }}
            lineHeight="25.6px"
            mx="auto"
            pb="8px"
            left={document.documentElement.dir === "rtl" ? "30px" : ""}
            right={document.documentElement.dir === "rtl" ? "" : "30px"}
            px={{
                md: "30px",
            }}
            ps={{
                xl: "12px",
            }}
            pt="8px"
            top="18px"
            w={{ sm: "calc(100vw - 30px)", xl: "calc(100vw - 75px - 275px)" }}
        >
            <Flex
                w="100%"
                flexDirection={{
                    sm: "column",
                    md: "row",
                }}
                alignItems={{ xl: "center" }}
            >
                <Box mb={{ sm: "8px", md: "0px" }}>
                    <Breadcrumb>
                        <BreadcrumbItem>
                            <BreadcrumbLink href="#">
                                Pages
                            </BreadcrumbLink>
                        </BreadcrumbItem>

                        <BreadcrumbItem>
                            <BreadcrumbLink href="#">
                                opa
                            </BreadcrumbLink>
                        </BreadcrumbItem>
                    </Breadcrumb>
                    {/* <Link
                        color={mainText}
                        href="#"
                        bg="inherit"
                        borderRadius="inherit"
                        fontWeight="bold"
                        _hover={{ color: { mainText } }}
                        _active={{
                            bg: "inherit",
                            transform: "none",
                            borderColor: "transparent",
                        }}
                        _focus={{
                            boxShadow: "none",
                        }}
                    >
                        {brandText}
                    </Link> */}
                </Box>
                <Box ms="auto" w={{ sm: "100%", md: "unset" }}>

                </Box>
            </Flex>
        </Flex>
    )
}

export default Navbar;
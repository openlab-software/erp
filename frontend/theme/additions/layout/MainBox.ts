import { defineStyleConfig } from '@chakra-ui/react';

export const MainBox = defineStyleConfig({
    baseStyle: {
        float: "right",
        maxWidth: "100%",
        overflow: "auto",
        position: "relative",
        maxHeight: "100%",
        transition: "all 0.33s cubic-bezier(0.685, 0.0473, 0.346, 1)",
        transitionDuration: ".2s, .2s, .35s",
        transitionProperty: "top, bottom, width",
        transitionTimingFunction: "linear, linear, ease",
    },
    variants: {
        main: {
            float: "right",
        },
    },
    defaultProps: {
        variant: "main",
    },
});

export const MainBoxContainer = defineStyleConfig({
    baseStyle: {
        ms: "auto",
        me: "auto",
        ps: "15px",
        pe: "15px",
    },
    variants: {
        main: {},
    },
    defaultProps: {
        variant: "main",
    },
});

export const MainBoxContent = defineStyleConfig({
    baseStyle: {
        ms: "auto",
        me: "auto",
        ps: "15px",
        pe: "15px",
    },
    variants: {
        main: {},
    },
    defaultProps: {
        variant: "main",
    },
});
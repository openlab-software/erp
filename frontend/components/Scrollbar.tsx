import { Scrollbars } from "react-custom-scrollbars-2";
import { useColorModeValue } from '@chakra-ui/react'


const renderThumbDark: React.FC<{ style: any }> = ({ style, ...props }) => {
  const thumbStyle = {
    borderRadius: 15,
    background: "rgba(222, 222, 222, .1)",
  };
  return <div style={{ ...style, ...thumbStyle }} {...props} />;
};
const renderThumbLight: React.FC<{ style: any }> = ({ style, ...props }) => {
  const thumbStyle = {
    borderRadius: 15,
    background: "rgba(48, 48, 48, .1)",
  };
  return <div style={{ ...style, ...thumbStyle }} {...props} />;
};

const Scrollbar: React.FC<React.PropsWithChildren> = ({ children }) => {
  return <Scrollbars
    autoHide
    renderTrackVertical={({ style, ...props }) => {
      const trackStyle = {
        position: "absolute",
        maxWidth: "100%",
        width: 6,
        transition: "opacity 200ms ease 0s",
        opacity: 0,
        bottom: 2,
        top: 2,
        borderRadius: 3,
        right: 0,
      };
      return <div style={{ ...style, ...trackStyle }} {...props} />;
    }}
    renderThumbVertical={useColorModeValue(
      renderThumbLight,
      renderThumbDark
    )}
    renderView={({ style, ...props }) => <div style={{ ...style, marginRight: -15 }} {...props} />}
  >
    {children}
  </Scrollbars>
};

export default Scrollbar;
import React, { Fragment } from 'react';

import MainBox from './MainBox';
import Sidebar from './Sidebar';

const Layout = (props: React.PropsWithChildren) => {
    return (
        <Fragment>
            <Sidebar />
            <MainBox w={{
                base: '100%',
                xl: 'calc(100% - 275px)'
            }}>
                {/* <Portal>
                    <Navbar />
                </Portal> */}

                <MainBox.Container>
                    <MainBox.Content>{props.children}</MainBox.Content>
                </MainBox.Container>
            </MainBox>
        </Fragment >
    )
}

export default Layout;
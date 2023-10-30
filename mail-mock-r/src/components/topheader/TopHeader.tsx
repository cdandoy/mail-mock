import React from "react";
import './TopHeader.scss'
import {Dropdown} from "react-bootstrap";

interface HeaderProps {
    toolbar?: React.ReactNode,
    search?: string,
    setSearch?: (search: string) => void,
    menu?: React.ReactNode
}

export function TopHeader({toolbar, search, setSearch, menu}: HeaderProps) {
    return <>
        <nav className="sb-topnav navbar navbar-expand navbar-dark bg-dark">
            <span className="navbar-brand ps-3">MailMock</span>
            <div className={"header-toolbar"}>
                {toolbar}
            </div>
            {setSearch &&
                <form className="d-none d-md-inline-block form-inline ms-auto me-0 me-md-3 my-2 my-md-0">
                    <div className="input-group">
                        <input className="form-control"
                               type="search"
                               placeholder="Filter"
                               aria-label="Search"
                               aria-describedby="btnNavbarSearch"
                               value={search}
                               onChange={e => setSearch(e.target.value.toLowerCase())}
                        />
                    </div>
                </form>
            }
            {menu &&
                <Dropdown>
                    <Dropdown.Toggle variant="dark">
                        <i className={"fa fa-cog"}/>
                    </Dropdown.Toggle>

                    <Dropdown.Menu>
                        {menu}
                    </Dropdown.Menu>
                </Dropdown>
            }
        </nav>
    </>
}

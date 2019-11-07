import React, {Component} from 'react';
import MessageSender from "./MessageSender";
import {Button, Container, Form, Input, ListGroup, ListGroupItem, Row, Col} from "reactstrap";
import {sendServerRequestWithBody} from "../../api/restfulAPI";
import ListNotifications from "./ListNotifications";
import ListMatches from "./ListMatches";



export default class HomePage extends Component {
    constructor(props) {
        super(props);

        this.getTableList = this.getTableList.bind(this);


        for(let key in props){
            console.log("Prop key: "+key+". Value: "+props[key]);
        }
        this.state={
            allNotifications: {},
            allInvites: {},
            showingNotifications: false,
            showingMatches: false,
            allMatches: {}
        }

    }

    render(){
        return(
            <Container>
                <Row>
                    <Col>
                    <h1>RollerBall HomePage</h1>
                    </Col>
                    <Col>
                        <Button onClick={() => this.props.setAppPage('login')}>Logout</Button>
                    </Col>
                </Row>
                <Row>
                    <Col>
                    <MessageSender token={this.props.token} serverPort={this.props.serverPort}/>
                    </Col>
                </Row>
                <Row>
                    <Button onClick={() => this.getTableList("notifications")}>View Notifications</Button>
                    <Button onClick={() => this.getTableList("ViewCurrentGames")}>View Current Games</Button>
                </Row>
                {this.renderNotifications()}
                {this.renderMatches()}
            </Container>
        );
    }


    getTableList(table){
        if(!this.state.showingNotifications) {
            const body = {
                token: this.props.token
            };

            sendServerRequestWithBody(table, body, this.props.serverPort).then(
                (response) => {
                    if (!response.body.message) {
                        if(table === "notifications") {
                            this.state.allNotifications = response.body;
                            this.state.showingNotifications = true;
                            this.setState(this.state);
                        }if(table === "ViewCurrentGames"){
                            this.state.allMatches = response.body;
                            this.state.showingMatches = true;
                            this.setState(this.state);
                        }
                    } else {
                        console.log("Did not work");
                    }
                }
            );
        }
        else
            this.setState({showingNotifications:false});
    }

    renderNotifications(){
        if(this.state.showingNotifications)
            return <ListNotifications ListNotifications={this.state.allNotifications} setAppPage={this.props.setAppPage}
                                      serverPort={this.props.serverPort} token={this.props.token} setAppState={this.props.setAppState}
            />;
        return null;
    }

    renderMatches(){
        if(this.state.showingMatches)
            return <ListMatches ListMatches={this.state.allMatches}
            />;
        return null;
    }

}
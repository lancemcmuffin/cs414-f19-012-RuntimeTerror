import React, {Component} from 'react';
import MessageSender from "./MessageSender";
import {Button, Container, Form, Input, ListGroup, ListGroupItem, Row, Col} from "reactstrap";
import {sendServerRequestWithBody} from "../../api/restfulAPI";
import ListNotifications from "./ListNotifications";
import ListMatches from "./ListMatches";



export default class HomePage extends Component {
    constructor(props) {
        super(props);

        this.getGamesList = this.getGamesList.bind(this);
        this.state={
            allInvites: {},
            showingNotifications: false,
            showingMatches: false,
            showingCompletedGames: false,
            allMatches: {},
            completedMatches: {},
            deregisterClicks: 0
        }
    }

    render(){
        return(
            <Container>
                <Row>
                    <Col>
                    <h1>Rollerball Home Page</h1>
                    </Col>
                    <Col>
                        <Button color="danger" onClick={() => this.logout()}>Logout</Button> &nbsp;
                        <Button color="outline-danger" onClick={() => this.deregisterStep()}>{this.getDeregisterMessage()}</Button>
                    </Col>
                </Row>
                <Row>
                    <Col>
                    <MessageSender token={this.props.token} serverPort={this.props.serverPort}/>
                    </Col>
                </Row>
                <Row>
                    <Button color="info" onClick={() => this.toggleNotifications()}>View Notifications</Button> &nbsp;
                    <Button color="secondary" onClick={() => this.getGamesList("ViewCurrentGames")}>View Current Games</Button> &nbsp;
                    <Button color="warning" onClick={() => this.getGamesList("CompletedGames")}>View Completed Games</Button>
                </Row>
                {this.renderNotifications()}
                {this.renderMatches()}
                {this.renderCompletedMatches()}
            </Container>
        );
    }

    toggleNotifications() {
        this.setState({showingNotifications: !this.state.showingNotifications});
    }

    getGamesList(table){
        if(table === 'ViewCurrentGames' || table === 'CompletedGames') {
            if(table === 'ViewCurrentGames' && this.state.showingMatches === true) {
                this.setState({showingMatches: false});
                return;
            }else if(table === "ViewCurrentGames"){
                let body = {
                    token: this.props.token,
                    finishedGames: false,
                    userID: this.props.token.id
                };
                this.sendRequest("ViewCurrentGames", body, "ViewCurrentGames");
            }
            if(table === 'CompletedGames' && this.state.showingCompletedGames === true) {
                this.setState({showingCompletedGames: false});
            }else if(table === "CompletedGames"){
                let body = {
                    token: this.props.token,
                    finishedGames: true,
                    userID: this.props.token.id
                };
                this.sendRequest("ViewCurrentGames", body, "CompletedGames");
            }
        }
    }

    sendRequest(table, body, updateTable){
        sendServerRequestWithBody(table, body, this.props.serverPort).then(
            (response) => {
                if (!response.body.message) {
                    if(updateTable === "CompletedGames") {
                        this.state.completedMatches = response.body;
                        this.state.showingCompletedGames = true;
                        this.setState(this.state);
                    }if(updateTable === "ViewCurrentGames"){
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


    renderNotifications(){
        if(this.state.showingNotifications)
            return <ListNotifications setAppPage={this.props.setAppPage} setAppState={this.props.setAppState}
                                      serverPort={this.props.serverPort} token={this.props.token}
            />;
        return null;
    }

    renderMatches(){
        if(this.state.showingMatches)
            return <ListMatches ListMatches={this.state.allMatches}
                                setAppState={this.props.setAppState}
                                setAppPage={this.props.setAppPage}
                                gameType={"CurrentGames"}
            />;
        return null;
    }

    renderCompletedMatches(){
        if(this.state.showingCompletedGames)
            return <ListMatches ListMatches={this.state.completedMatches}
                                setAppState={this.props.setAppState}
                                setAppPage={this.props.setAppPage}
                                gameType={"CompletedGames"}
            />;
        return null;
    }

    logout(){
        this.props.setAppPage('login');
    }

    deregisterStep(){
        if(this.state.deregisterClicks < 5){
            this.setState({deregisterClicks: this.state.deregisterClicks+1});
        }
        else{
            sendServerRequestWithBody("deregister", {token:this.props.token}, this.props.serverPort).then(
                ()=>this.logout()
            );
        }
    }

    getDeregisterMessage(){
        let message = "Deregister";
        for(let i=this.state.deregisterClicks;i>0;i--){
            message = "Really "+message+"?";
        }
        return message;
    }
}
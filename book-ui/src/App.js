import React from 'react';
import './App.css';
import Client from "./rsocket/Client";

class App extends React.Component {

    constructor(props) {
        super(props);
        const client = new Client();
        this.state = {client, books: [], shouldPause: false, subscriber: null};
    }

    async connect() {
        const socket = await this.state.client.connect();
        this.setState({socket});
    }

    sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    stream() {
        const {socket} = this.state;
        socket.requestStream({
            metadata: String.fromCharCode("/all-books".length) + "/all-books",
        }).subscribe({
            onSubscribe: (subs) => {
                subs.request(1);
                this.setState({subscriber: subs});
            },
            onNext: async (payload) => {
                const books = this.state.books;
                books.push(payload.data);
                this.setState({books: books});
                await this.sleep(1000);
                if (!this.state.shouldPause) {
                    this.state.subscriber.request(1);
                }
            },
            onComplete: () => {

            },
            onError: (err) => {

            }
        })
    }

    pause() {
        this.setState({shouldPause: true});
    }

    resume() {
        this.setState({shouldPause: false});
        this.state.subscriber.request(1);
    }

    render() {
        const {socket, books, shouldPause, subscriber} = this.state;
        return (
            <div className="App">
                <header className="App-header">
                    <p>
                        RSocket
                    </p>
                    {!socket && <button onClick={() => this.connect()}>Connect</button>}
                    {socket && !subscriber && <button onClick={() => this.stream()}>Stream</button>}
                    {socket && !shouldPause && <button onClick={() => this.pause()}>Pause</button>}
                    {socket && shouldPause && <button onClick={() => this.resume()}>Resume</button>}
                </header>
                <main className="contents">
                    {books && this.renderBooks()}
                </main>
            </div>
        );
    }

    renderBooks() {
        const {books} = this.state;
        return (
            <div>
                {
                    books.map(book => {
                        return (<div key={Date.now() + book.id}>{JSON.stringify(book)}</div>)
                    })
                }
            </div>
        );
    }
}

export default App;

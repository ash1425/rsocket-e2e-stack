import {IdentitySerializer, JsonSerializer, RSocketClient} from "rsocket-core";
import RSocketWebSocketClient from "rsocket-websocket-client/build";
import {APPLICATION_JSON} from "rsocket-core/build";
import {MESSAGE_RSOCKET_ROUTING} from "rsocket-core/build/WellKnownMimeType";

export default class Client {
    client: RSocketClient = new RSocketClient({
        serializers: {
            data: JsonSerializer,
            metadata: IdentitySerializer,
        },
        setup: {
            keepAlive: 60000,
            lifetime: 180000,
            dataMimeType: APPLICATION_JSON._string,
            metadataMimeType: MESSAGE_RSOCKET_ROUTING._string,
        },
        transport: new RSocketWebSocketClient({url: "ws://localhost:7777"}),
    });

    async connect() {
        return await this.client.connect();
    }

    close() {
        this.client.close();
    }
}

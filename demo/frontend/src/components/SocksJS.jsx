import React from "react"
import SockJS from "sockjs-client"
import Stomp from "stompjs"
import PropTypes from "prop-types"
import _ from 'lodash'

class SockJsClient extends React.Component {

  static defaultProps = {
    onConnect: () => {},
    onDisconnect: () => {},
    getRetryInterval: (count) => 1000 * count,
    headers: {},
    autoReconnect: true,
    debug: false,
    sessionId: null
  }

  static propTypes = {
    url: PropTypes.string.isRequired,
    topics: PropTypes.array.isRequired,
    onConnect: PropTypes.func,
    onDisconnect: PropTypes.func,
    getRetryInterval: PropTypes.func,
    onMessage: PropTypes.func.isRequired,
    headers: PropTypes.object,
    autoReconnect: PropTypes.bool,
    debug: PropTypes.bool,
    sessionId: PropTypes.string
  }

  constructor(props) {
    super(props)

    this.state = {
      connected: false
    };

    this.subscriptions = new Map()
    this.retryCount = 0
  }

  componentDidMount() {
    this.connect();
  }

  componentWillUnmount() {
    this.disconnect()
  }

  componentWillReceiveProps(nextProps) {
    if (this.state.connected) {
      // new topics
      _.pullAll(nextProps.topics.slice(), this.props.topics)
       .forEach(topic => this.subscribe(topic))
      
      // obsolete topics
      _.pullAll(this.props.topics.slice(), nextProps.topics)
       .forEach(topic => this.unsubscribe(topic))
    } else {
      this.props.topics = _.intersection(this.props.topics, nextProps.topics)
    }
  }

  render() {
    return (<div></div>)
  }

  _initStompClient = () => {
    // Websocket held by stompjs can be opened only once
    this.client = this.props.sessionId 
                  ? Stomp.over(new SockJS(this.props.url, [], {sessionId: () => this.props.sessionId}))
                  : Stomp.over(new SockJS(this.props.url))
                  
    if (!this.props.debug) {
      this.client.debug = () => {};
    }
  }

  _cleanUp = () => {
    this.setState({ connected: false });
    this.retryCount = 0;
    this.subscriptions.clear();
  }

  _log = (msg) => {
    if (this.props.debug) {
      console.log(msg);
    }
  }

  connect = () => {
    this._initStompClient();
    this.client.connect(this.props.headers, () => {
      this.setState({ connected: true });
      this.props.topics.forEach((topic) => {
        this.subscribe(topic);
      });
      this.props.onConnect();
    }, (error) => {
      if (this.state.connected) {
        this._cleanUp();
        // onDisconnect should be called only once per connect
        this.props.onDisconnect();
      }
      if (this.props.autoReconnect) {
        this._timeoutId = setTimeout(this.connect, this.props.getRetryInterval(this.retryCount++));
      }
    });
  }

  disconnect = () => {
    // On calling disconnect explicitly no effort will be made to reconnect
    // Clear timeoutId in case the component is trying to reconnect
    if (this._timeoutId) {
      clearTimeout(this._timeoutId);
    }
    if (this.state.connected) {
      this.subscriptions.forEach((subid, topic) => {
        this.unsubscribe(topic);
      });
      this.client.disconnect(() => {
        this._cleanUp();
        this.props.onDisconnect();
        this._log("Stomp client is successfully disconnected!");
      });
    }
  }

  subscribe = (topic) => {
    let sub = this.client.subscribe(topic, (msg) => {
      this.props.onMessage(topic, JSON.parse(msg.body), msg.headers);
    });
    this.subscriptions.set(topic, sub);
  }

  unsubscribe = (topic) => {
    let sub = this.subscriptions.get(topic);
    sub.unsubscribe();
    this.subscriptions.delete(topic);
  }

  // Below methods can be accessed by ref attribute from the parent component
  sendMessage = (topic, msg, opt_headers = {}) => {
    if (this.state.connected) {
      this.client.send(topic, opt_headers, msg);
    } else {
      console.error("Send error: SockJsClient is disconnected");
    }
  }
}

export default SockJsClient;
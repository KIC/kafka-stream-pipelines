var path = require('path');
var webpack = require('webpack');
var HtmlWebpackPlugin = require('html-webpack-plugin');
var LodashModuleReplacementPlugin = require('lodash-webpack-plugin');

module.exports = {
  devtool:'source-map',
  devServer: {
    port: 3030,
    proxy: {
      "/api": "http://localhost:8080",
      "/sockjs-node": {
        target: "http://localhost:8080",
        ws: true
      },
      "/": {
        target: "ws://localhost:8080",
        ws: true
      },
    }
  },
  node: {
    fs: "empty",
    net: "empty",
    tls: "empty",
    console: true
  },
  entry: [
    './src/index'
  ],
  output: {
    path: path.join(__dirname, 'dist'),
    filename: 'bundle.js',
    publicPath: 'http://localhost:3030/'
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin(),
    new LodashModuleReplacementPlugin,
    new HtmlWebpackPlugin({
        title: 'Boot React',
        template: path.join(__dirname, 'assets/index-template.html')
    }),
    new webpack.DefinePlugin({
      'process.env': {
          // This has effect on the react lib size
          NODE_ENV: JSON.stringify('production'),
      },
    })
  ],
  resolve: {
    extensions: ['.js', '.jsx']
  },
  module: {
    rules: [{
      test: /\.jsx?$/,
      use: ['babel-loader'],
      include: path.join(__dirname, 'src')
    },
    {
      test: /.*gl-matrix.*\.js$/,
      use:['babel-loader']
    },
    { 
      test: /\.(png|woff|woff2|eot|ttf|svg)$/, 
      loader: ['url-loader?limit=100000']
    },
    {
      test:/\.css$/, 
      use:['style-loader','css-loader']
    },
    {
      test: /\.js$/,
      use:['ify-loader']
    }]
  }
};

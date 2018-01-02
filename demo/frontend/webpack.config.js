var path = require('path');
var webpack = require('webpack');
var HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
  devtool: 'eval',
  devServer: {
    port: 3030,
    proxy: {
      "/api": "http://localhost:8080"
    }
  },
  node: {
    fs: "empty"
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
    new webpack.optimize.DedupePlugin(),
    new HtmlWebpackPlugin({
        title: 'Boot React',
        template: path.join(__dirname, 'assets/index-template.html')
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
      test: /\.(png|woff|woff2|eot|ttf|svg)$/, 
      loader: 'url-loader?limit=100000' 
    },
    {
      test:/\.css$/, 
      use:['style-loader','css-loader']
    },
    {
      test: /\.js$/,
      use: 'ify-loader',
    }]
  }
};

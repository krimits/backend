# Food Delivery App

A distributed food delivery system implemented in Java using a MapReduce-like architecture for efficient store searching and order processing.

## Architecture

The system follows a distributed architecture with the following components:

- **Master**: Central coordinator that manages communication between clients and workers
- **Workers**: Distributed nodes that process store data and handle operations
- **Reducer**: Aggregates results from multiple workers
- **Clients**: End users who can search for stores and place orders
- **Manager**: Administrative interface for store and product management

## Features

### For Customers
- Search for nearby stores based on location (within specified radius)
- Filter stores by:
  - Food categories
  - Minimum rating
  - Price category
- View store products and place orders
- Rate stores

### For Store Managers
- Add new stores with JSON configuration
- Add/Update products
- Remove products or update quantities
- View sales statistics by:
  - Store type
  - Product category

## Code Structure

### Core Components

- `Master.java`: Main server that coordinates communication
- `Worker.java`: Distributed processing nodes
- `Reducer.java`: Result aggregation service
- `Client.java`: Customer interface
- `Manager.java`: Store management interface

### Models

- `Store.java`: Store entity with location and rating info
- `Product.java`: Product details and inventory
- `Purchase.java`: Order information
- `MapReduceRequest.java`: Search/filter request format

### Supporting Classes

- `Actions.java`: Request handling logic for Master
- `WorkerActions.java`: Request processing logic for Workers
- `ReducerActions.java`: Aggregation logic for Reducer
- `StoreData.java`: JSON parsing and store data management

## Data Format

Stores are configured using JSON files with the following structure:

```json
{
  "StoreName": "store name",
  "Latitude": 00.000000,
  "Longitude": 00.000000,
  "FoodCategory": "category",
  "Stars": 0,
  "NoOfVotes": 0,
  "StoreLogo": "",
  "Products": [
    {
      "ProductName": "product name",
      "ProductType": "category",
      "Available Amount": 0,
      "Price": 0.00
    }
  ]
}
```

## Getting Started

1. Start the Reducer server:
```bash
java Reducer
```

2. Start one or more Worker nodes:
```bash
java Worker <port>
```

3. Start the Master server with Worker addresses:
```bash
java Master <worker1_ip> <worker1_port> <worker2_ip> <worker2_port> ...
```

4. Run the Client or Manager application:
```bash
java Client
# or
java Manager
```

## Dependencies

- Java 8 or higher
- json-simple-1.1.1.jar for JSON processing

## Configuration

The system uses the following default ports:
- Master: 4321
- Reducer: 4325 
- Workers: User-specified ports passed as arguments

## Contributing

Feel free to submit issues and enhancement requests.

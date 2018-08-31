# SpiriFit FGApp
This user interface used to support worker to do daily work at the warehouse. There is eight function includes Receiving, Moving, Search, Picking, Shipping, Daily Report, Replenishment, Daily Shipping. The application support
* **Receiving** use to received data from the barcode scanner and send data to our database.  
* **Moving** used to assign each item to another location. 
* **Search** allows the worker to find how many stocks on the warehouse according to model and location.
* **Picking** allows the worker to find where is location have the stock base on sales order information.
* **Shipping** used to ship items according to the sales order. Each sales order has the serial number and tracking number information. All the information be sent to our server.
* **Daily Inventory Report** It shows how many receiving and shipping items quantity information every day.
* **Replenishment** When the quantity of model less than min quantity which we preset. The model will display on the report.
* **Daily Shipping** It shows daily shipping information with sales order on the report. 
# Develop tool
* Operation System: Window 10 Pro (Destop and Tablet)  
* Develop IDE tool: Eclipse 4.7.2
* Java SDK : 1.7.2
# 3rd party libray
* Retrofit 2.3.0  
* OKHttp 3.8.0  
* java mail 1.4.4  

# Screenshots
* **Application**  
![alt text](https://github.com/geminihsu/SpiritFitFinishGoodsClient/blob/master/screenshot/index.png)  
* **Receiving**  
**1. Container List**  
![alt text](https://github.com/geminihsu/SpiritFitFinishGoodsClient/blob/master/screenshot/container%20list.png)  
**2. Scanning panel**  
![alt text](https://github.com/geminihsu/SpiritFitFinishGoodsClient/blob/master/screenshot/Scan%20panel.png)  
* **Moving**  
![alt text](https://github.com/geminihsu/SpiritFitFinishGoodsClient/blob/master/screenshot/move%20panel.png)  
* **Search**  
![alt text](https://github.com/geminihsu/SpiritFitFinishGoodsClient/blob/master/screenshot/search%20function.png)  
* **Picking**  
![alt text](https://github.com/geminihsu/SpiritFitFinishGoodsClient/blob/master/screenshot/picking.png)  
* **Shipping**  
![alt text](https://github.com/geminihsu/SpiritFitFinishGoodsClient/blob/master/screenshot/shipping%20scan%20panel.png)  
* **Daily Inventory Report**  
![alt text](https://github.com/geminihsu/SpiritFitFinishGoodsClient/blob/master/screenshot/daily%20report.png)  
* **Daily Shipping Report**  
![alt text](https://github.com/geminihsu/SpiritFitFinishGoodsClient/blob/master/screenshot/daily%20shipping%20function.png)  

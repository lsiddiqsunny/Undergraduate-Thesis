UPDATE Customers
SET ContactName='Juan'
WHERE Country like 'Mexico';

UPDATE Customers
SET ContactName='Juan';

 SELECT * FROM Customers
WHERE NOT Country='Germany' ;

 SELECT name, salary
FROM employees e, manager m
WHERE e.salary > m.salary;


 SELECT  ID, NAME, AMOUNT, DATE
   FROM CUSTOMERS, ORDERS;

SELECT B.FirstName AS FirstName1, B.LastName AS LastName1,
       A.FirstName AS FirstName2, A.LastName AS LastName2,
       B.City, B.Country
  FROM Customer A JOIN Customer B
    ON A.Id <> B.Id
   AND A.City = B.City
   AND A.Country = B.Country
 ORDER BY A.Country;

SELECT C.FirstName, C.LastName, C.Country AS CustomerCountry,
       S.Country AS SupplierCountry, S.CompanyName
  FROM Customer C FULL JOIN Supplier S
    ON C.Country = S.Country
 ORDER BY C.Country, S.Country;

SELECT O.OrderNumber, CONVERT(date,O.OrderDate) AS Date,
       P.ProductName, I.Quantity, I.UnitPrice
  FROM Order as O
  JOIN OrderItem as I ON O.Id = I.OrderId
  JOIN Product as P ON P.Id = I.ProductId
ORDER BY O.OrderNumber;

SELECT e.last_name,
       e.department_id,
       d.department_name
FROM   employees e
       LEFT OUTER JOIN department d
         ON ( e.department_id = d.department_id );



INSERT INTO Customers (CustomerName, ContactName, Address, City, PostalCode, Country)
VALUES ('Cardinal', 'Tom B. Erichsen', 'Skagen 21', 'Stavanger', '4006', 'Norway');
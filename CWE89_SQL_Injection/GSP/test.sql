--problem in 116, 150, 151, 152, 158, 349 (inner join ... on), 

select a1.item_id, a1.item_name, a3.member_no, a3.nickname, MAX(a2.time), a5.status, a1.done, a1.getter_id.member_no, a1.getter_rate, a1.giver_rate, MIN(a2.readed) 
from MessageContextBean a2 JOIN a2.itemBean a1 LEFT JOIN a2.memberBean_speaker a3 LEFT JOIN RequestListBean a5 on a1.item_id = a5.item_id AND a3.member_no = a5.requester_id on a1.member_id.member_no = member_id AND a3.member_no <> member_id
GROUP BY a1.item_id, a1.item_name, a3.member_no, a3.nickname, a5.status, a1.done, a1.getter_id.member_no, a1.getter_rate, a1.giver_rate ORDER BY MAX(a2.time) DESC;

"select  * FROM movie m JOIN order_movie om ON m.movie_id = om.movie_id JOIN user_order uo ON uo.order_id = om.order_id JOIN user u ON u.user_id = uo.user_id JOIN categories c ON c.category_id = m.category_id WHERE u.user_id = user_id AND c.name = category AND uo.confirm_no = 1 ORDER BY uo.date_create desc;

SELECT mp.* FROM movie_pic mp JOIN movie m on m.movie_id = mp.movie_id WHERE m.movie_id = id;

select * 
FROM movie m 
JOIN order_movie om ON m.movie_id = om.movie_id JOIN user_order uo ON uo.order_id = om.order_id JOIN user u ON u.user_id = uo.user_id 
WHERE u.user_id = user_id AND uo.confirm_no = 1 ORDER BY uo.date_create desc;

INSERT INTO Resurse_Financiare  VALUES( 'rf.getDescriere()', 'rf.getSuma()',tip);
 
 
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
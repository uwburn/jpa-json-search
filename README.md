# JPA JSON Search
Build JPA queries with JSON

## JSON Example
```json
{
	"filter": [
		{ "$or": [
		  { "sequence": { "$gt": 0 } },
		  { "sequence": { "$eq": -10 } }
		  { "sequence": { "$eq": -20 } },
		  { "$and": [
			{ "sequence": { "$gte": 50 } },
			{ "name": { "$eq": "John" } },
		  ]}
		]},
		{ "surname": { "$lk": "%son" } }
	],
	"page": 0,
	"pageSize": 10,
	"sort": [
		{ "sequence": "ASC" }
	]
}
```

Please note that the "root" filter element is implictly and `AND` container.

## Credits
Thanks for the idea to [Narmer23](https://github.com/Narmer23).

Thanks for the support to [Narmer23](https://github.com/Narmer23) and [sandrotaje](https://github.com/sandrotaje).

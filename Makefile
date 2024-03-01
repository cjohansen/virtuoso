resources/fontawesome-icons:
	clojure -Sdeps "{:deps {no.cjohansen/fontawesome-clj {:mvn/version \"2023.10.26\"} \
	clj-http/clj-http {:mvn/version \"3.12.3\"} \
	hickory/hickory {:mvn/version \"0.7.1\"}}}" \
	-M -m fontawesome.import :download resources 6.4.2

node_modules:
	npm install

tailwind: resources/fontawesome-icons node_modules
	npx tailwindcss -i ./src/main.css -o ./resources/public/tailwind.css --watch

.PHONY: tailwind

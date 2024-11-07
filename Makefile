node_modules:
	npm install

resources/mattilsynet-design:
	mkdir -p resources/mattilsynet-design

resources/public/mtds:
	mkdir -p resources/public/mtds

resources/mattilsynet-design/css-modules.edn: resources/mattilsynet-design
	clojure -T:build build-css-modules

resources/mattilsynet-design/icons: resources/mattilsynet-design
	clojure -T:build export-all-svgs

resources/public/mtds/style.css: resources/public/mtds
	cp -r node_modules/@mattilsynet/design/mtds/style.css resources/public/mtds/styles.css

clean:
	rm -fr resources/public resources/mattilsynet-design

update:
	rm -fr resources/public/mtds
	npm update @mattilsynet/design

refresh: clean update resources/mattilsynet-design/css-modules.edn resources/mattilsynet-design/icons resources/public/mtds/style.css

mattilsynet-designsystem.jar: src/mattilsynet/* resources/mattilsynet-design/* resources/public/mtds/*
	rm -f mattilsynet-designsystem.jar && clojure -A:dev -M:jar

deploy: mattilsynet-designsystem.jar
	mvn deploy:deploy-file -Dfile=mattilsynet-designsystem.jar -DrepositoryId=clojars -Durl=https://clojars.org/repo -DpomFile=pom.xml

.PHONY: update clean refresh deploy

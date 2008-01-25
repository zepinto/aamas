import os

startnumber = 0
values1 = ['ec.multiobjective.SubsumptionMultiObjectiveFitness','ec.gep.WeightedFitness']
values2 = ['1','2','3','4']
values3 = ['50','100','250','500','1000']
values4 = ['50','100','250','500','1000']
values5 = [
'gep.species.symbolset.functionsize = 3\n\
gep.species.symbolset.function.0 = And\n\
gep.species.symbolset.function.0.weight = 1\n\
gep.species.symbolset.function.1 = Or\n\
gep.species.symbolset.function.1.weight = 1\n\
gep.species.symbolset.function.2 = Not\n\
gep.species.symbolset.function.2.weight = 1\n',
'gep.species.symbolset.functionsize = 5\n\
gep.species.symbolset.function.0 = And\n\
gep.species.symbolset.function.0.weight = 1\n\
gep.species.symbolset.function.1 = Or\n\
gep.species.symbolset.function.1.weight = 1\n\
gep.species.symbolset.function.2 = Not\n\
gep.species.symbolset.function.2.weight = 1\n\
gep.species.symbolset.function.3 = Nand\n\
gep.species.symbolset.function.3.weight = 1\n\
gep.species.symbolset.function.4 = Xor\n\
gep.species.symbolset.function.4.weight = 1\n']

f = open('params/run.template', 'r')
contents = f.read()
f.close()
count = 0
for i1 in range(0, len(values1)):
	for i2 in range(0, len(values2)):
		for i3 in range(0, len(values3)):
			for i4 in range(0, len(values4)):
				for i5 in range(0, len(values5)):
					contents2 = contents.replace('{1}', values1[i1])
					contents2 = contents2.replace('{2}', values2[i2])
					contents2 = contents2.replace('{3}', values3[i3])
					contents2 = contents2.replace('{4}', values4[i4])
					contents2 = contents2.replace('{5}', values5[i5])
					contents2 = contents2.replace('{count}', str(count))
					f = open('params/run%d.params'%count, 'w')
					f.write(contents2)
					f.close()
					count = count + 1
					#os.system('java -jar gep.jar -file params/run%d.params' % count)
	
f = open('run.sh.template', 'r')
contents = f.read()
f.close()

for i in range(0, count):
	c = contents.replace('{run}', str(count))
	f = open('run%d.sh' % i, 'w')
	f.write(c)



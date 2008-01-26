import os

RUN_TESTS = 1
GENERATE_SH = 0

startnumber = 0

fitness_funcs = ['edu.mapi.aamas.ge.fitness.SubsumptionFitness','edu.mapi.aamas.ge.fitness.WeightedFitness']

problems = ['1','2','3','4']

operators = [
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

def replaceParam(contents, param, values):
	ret = []
	for i in range(0, len(contents)):
		for j in range(0, len(values)):
			ret = ret + [contents[i].replace(param, values[j])]
	return ret



f = open('params/run.template', 'r')
contents = f.read()
f.close()
runs = [contents]
runs = replaceParam(runs, '{fitness_func}', ['ec.gep.WeightedFitness'])
runs = replaceParam(runs, '{subs_threshold}', ['1.0'])
runs = replaceParam(runs, '{subs_maxloss}', ['0.0'])
runs = replaceParam(runs, '{acc_weight}', ['1.0', '0.98'])#, '0.95', '0.75'])
runs = replaceParam(runs, '{problem}', ['2'])#problems)
runs = replaceParam(runs, '{operators}', [operators[0]])
runs = replaceParam(runs, '{generations}', ['50'])
runs = replaceParam(runs, '{population}', ['100'])

print "Generating %d params files" % len(runs)

for i in range(0, len(runs)):
	runs[i] = runs[i].replace('{count}', str(i+startnumber))
	f = open('params/run%d.params'%(i+startnumber), 'w')
	f.write(runs[i])
	f.close()
	if RUN_TESTS:
		os.system('java -jar gep.jar -file params/run%d.params' % (i+startnumber))

if RUN_TESTS:
	os.system('rm stat/concat.txt')
	os.system('cat stat/?*.txt > stat/concat.txt')
	f = open('stat/concat.txt', 'r')
	contents = f.read()
	f.close()
	firstline = contents.split('\n')[0]
	contents = firstline + contents.replace(firstline, '')
	f = open('stat/concat.txt', 'w')
	f.write(contents)
	f.close()


if GENERATE_SH:
	f = open('run.sh.template', 'r')
	contents = f.read()
	f.close()

	for i in range(0, len(runs)):
		c = contents.replace('{run}', str(i))
		f = open('run%d.sh' % i, 'w')
		f.write(c)


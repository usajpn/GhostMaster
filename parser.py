import sys

def parse_log_file(f_name):
	f = open(f_name)
	line = f.readline()
	while line:
		if 'TIME' in line:
			a = line.split(':')
			print(a[1].rstrip())
		line = f.readline()	
	f.close()

if __name__ == "__main__":
	f_name = sys.argv[1]
	parse_log_file(f_name)
